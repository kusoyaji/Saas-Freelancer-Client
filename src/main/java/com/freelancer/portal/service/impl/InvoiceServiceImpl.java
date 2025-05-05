package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.InvoiceDto;
import com.freelancer.portal.dto.InvoiceItemRequestDto;
import com.freelancer.portal.dto.InvoiceItemResponseDto;
import com.freelancer.portal.dto.InvoiceRequestDto;
import com.freelancer.portal.dto.InvoiceResponseDto;
import com.freelancer.portal.dto.PaymentResponseDto;
import com.freelancer.portal.exception.ResourceNotFoundException;
import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.FileMetadata;
import com.freelancer.portal.model.Invoice;
import com.freelancer.portal.model.InvoiceItem;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.ClientRepository;
import com.freelancer.portal.repository.FileMetadataRepository;
import com.freelancer.portal.repository.InvoiceItemRepository;
import com.freelancer.portal.repository.InvoiceRepository;
import com.freelancer.portal.repository.ProjectRepository;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.FileService;
import com.freelancer.portal.service.InvoiceService;
import com.freelancer.portal.service.NotificationService;
import com.freelancer.portal.service.PaymentService;
import com.freelancer.portal.service.ProjectService;
import com.freelancer.portal.model.Notification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileService fileService;
    private final ClientRepository clientRepository;
    private final SecurityUtils securityUtils;
    private final ProjectService projectService;
    private final jakarta.persistence.EntityManager entityManager;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> getAllInvoices(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Page<Invoice> invoices = invoiceRepository.findAllByFreelancer(currentUser, pageable);
        return invoices.map(this::mapToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> getInvoicesByClient(Long clientId, Pageable pageable) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        
        User currentUser = securityUtils.getCurrentUser();
        if (!client.getFreelancer().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }
        
        Page<Invoice> invoices = invoiceRepository.findAllByClient(client, pageable);
        return invoices.map(this::mapToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> getInvoicesByStatus(String statusStr, Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice.Status status;
        try {
            status = Invoice.Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr);
        }
        
        Page<Invoice> invoices = invoiceRepository.findAllByFreelancerAndStatus(currentUser, status, pageable);
        return invoices.map(this::mapToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = securityUtils.getCurrentUser();
        List<Invoice> invoices = invoiceRepository.findAllByFreelancerAndIssueDateBetween(
                currentUser, startDate, endDate);
        return invoices.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByProject(Long projectId, Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        Project project = projectRepository.findByIdAndFreelancer(projectId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        
        return invoiceRepository.findByProject(project, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        if (!isInvoiceOwner(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        return mapToResponseDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceDetailById(Long id) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        // Verify the invoice belongs to a project owned by the current user
        if (!invoice.getProject().getFreelancer().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        return mapToDto(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceRequestDto invoiceRequestDto) {
        User currentUser = securityUtils.getCurrentUser();
        
        // Fetch the project if it's provided
        Project project = null;
        if (invoiceRequestDto.getProjectId() != null) {
            project = projectRepository.findById(invoiceRequestDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + invoiceRequestDto.getProjectId()));
            
            // Verify project ownership
            if (!project.getFreelancer().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Project not found with id: " + invoiceRequestDto.getProjectId());
            }
        }
        
        // Get client and validate ownership
        Client client = clientRepository.findById(invoiceRequestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + invoiceRequestDto.getClientId()));
        
        if (!client.getFreelancer().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Client not found with id: " + invoiceRequestDto.getClientId());
        }
        
        // Generate invoice number if not provided
        String invoiceNumber = invoiceRequestDto.getInvoiceNumber();
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            invoiceNumber = generateInvoiceNumber();
        }
        
        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setIssueDate(invoiceRequestDto.getIssueDate());
        invoice.setDueDate(invoiceRequestDto.getDueDate());
        invoice.setStatus(invoiceRequestDto.getStatus() != null ? invoiceRequestDto.getStatus() : Invoice.Status.DRAFT);
        invoice.setNotes(invoiceRequestDto.getNotes());
        invoice.setClient(client);
        invoice.setFreelancer(currentUser);
        invoice.setProject(project);
        invoice.setTaxRate(invoiceRequestDto.getTaxRate());
        invoice.setCurrency(invoiceRequestDto.getCurrency() != null ? invoiceRequestDto.getCurrency() : "USD");
        
        // Set supplied values for totals if provided
        if (invoiceRequestDto.getSubTotal() != null) {
            invoice.setSubtotal(invoiceRequestDto.getSubTotal());
        }
        if (invoiceRequestDto.getTaxAmount() != null) {
            invoice.setTaxAmount(invoiceRequestDto.getTaxAmount());
        }
        
        // Save the invoice first to generate ID
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Process invoice items
        if (invoiceRequestDto.getItems() != null && !invoiceRequestDto.getItems().isEmpty()) {
            List<InvoiceItem> items = new ArrayList<>();
            
            for (InvoiceItemRequestDto itemDto : invoiceRequestDto.getItems()) {
                InvoiceItem item = new InvoiceItem();
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setAmount(itemDto.getQuantity().multiply(itemDto.getUnitPrice()));
                item.setInvoice(savedInvoice);
                items.add(item);
            }
            
            // Properly set the items on the invoice to maintain the relationship
            savedInvoice.setItems(items);
            
            // Save the invoice with its items in a single transaction
            savedInvoice = invoiceRepository.save(savedInvoice);
        }
        
        // Calculate all totals after items are saved
        if (savedInvoice.getSubtotal() == null || savedInvoice.getSubtotal().compareTo(BigDecimal.ZERO) == 0) {
            calculateInvoiceTotals(savedInvoice);
            savedInvoice = invoiceRepository.save(savedInvoice);
        }
        
        // Get a fresh copy of the invoice with all relationships loaded
        Invoice refreshedInvoice = invoiceRepository.findById(savedInvoice.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found after save"));
        
        // Create notification for the new invoice
        notificationService.createInvoiceNotification(refreshedInvoice, Notification.NotificationType.INVOICE_CREATED);
        
        return mapToResponseDto(refreshedInvoice);
    }

    @Override
    @Transactional
    public InvoiceResponseDto updateInvoice(Long id, InvoiceRequestDto invoiceRequestDto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        if (!isInvoiceOwner(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        // Cannot edit invoices that are already paid
        if (Invoice.Status.PAID.equals(invoice.getStatus())) {
            throw new IllegalStateException("Cannot update a paid invoice");
        }
        
        // Update invoice fields
        invoice.setInvoiceNumber(invoiceRequestDto.getInvoiceNumber());
        invoice.setIssueDate(invoiceRequestDto.getIssueDate());
        invoice.setDueDate(invoiceRequestDto.getDueDate());
        invoice.setStatus(invoiceRequestDto.getStatus());
        invoice.setNotes(invoiceRequestDto.getNotes());
        invoice.setTaxRate(invoiceRequestDto.getTaxRate());
        
        if (invoiceRequestDto.getCurrency() != null) {
            invoice.setCurrency(invoiceRequestDto.getCurrency());
        }
        
        // If client is changed, validate ownership
        if (!invoice.getClient().getId().equals(invoiceRequestDto.getClientId())) {
            Client client = clientRepository.findById(invoiceRequestDto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + invoiceRequestDto.getClientId()));
            
            User currentUser = securityUtils.getCurrentUser();
            if (!client.getFreelancer().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Client not found with id: " + invoiceRequestDto.getClientId());
            }
            
            invoice.setClient(client);
        }
        
        // If project is provided, update and validate
        if (invoiceRequestDto.getProjectId() != null && 
            (invoice.getProject() == null || !invoice.getProject().getId().equals(invoiceRequestDto.getProjectId()))) {
            
            Project project = projectRepository.findById(invoiceRequestDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + invoiceRequestDto.getProjectId()));
            
            User currentUser = securityUtils.getCurrentUser();
            if (!project.getFreelancer().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Project not found with id: " + invoiceRequestDto.getProjectId());
            }
            
            invoice.setProject(project);
        }
        
        // Clear existing items and add new ones
        invoice.getItems().clear();
        
        List<InvoiceItem> items = new ArrayList<>();
        for (InvoiceItemRequestDto itemDto : invoiceRequestDto.getItems()) {
            InvoiceItem item = new InvoiceItem();
            item.setDescription(itemDto.getDescription());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            
            // Check if project is provided and validate
            if (itemDto.getProjectId() != null) {
                if (!projectService.isProjectOwner(itemDto.getProjectId())) {
                    throw new ResourceNotFoundException("Project not found with id: " + itemDto.getProjectId());
                }
                // Removed the call to item.setProjectId as this method doesn't exist
            }
            
            item.setInvoice(invoice);
            items.add(item);
        }
        
        invoice.setItems(items);
        
        // Calculate totals
        calculateInvoiceTotals(invoice);
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        return mapToResponseDto(updatedInvoice);
    }

    @Override
    @Transactional
    public InvoiceDto updateInvoiceStatus(Long id, Invoice.Status status) {
        User currentUser = securityUtils.getCurrentUser();
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        // Verify the invoice belongs to a project owned by the current user
        if (!existingInvoice.getProject().getFreelancer().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        existingInvoice.setStatus(status);
        
        // If status is PAID, set paid date to now
        if (status == Invoice.Status.PAID && existingInvoice.getPaidDate() == null) {
            existingInvoice.setPaidDate(LocalDate.now());
        }
        
        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);
        return mapToDto(updatedInvoice);
    }

    @Override
    @Transactional
    public InvoiceResponseDto markInvoiceAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        if (!isInvoiceOwner(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        invoice.setStatus(Invoice.Status.PAID);
        invoice.setPaidDate(LocalDate.now());
        
        // Update the amountPaid to match the total amount
        if (invoice.getAmount() != null) {
            invoice.setAmountPaid(invoice.getAmount());
            invoice.setAmountDue(BigDecimal.ZERO);
        }
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        // Create notification for the paid invoice
        notificationService.createInvoiceNotification(updatedInvoice, Notification.NotificationType.INVOICE_PAID);
        
        return mapToResponseDto(updatedInvoice);
    }

    @Override
    @Transactional
    public InvoiceResponseDto markInvoiceAsSent(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        if (!isInvoiceOwner(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        invoice.setStatus(Invoice.Status.SENT);
        invoice.setSentDate(LocalDate.now());
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        // Create notification for the sent invoice
        notificationService.createInvoiceNotification(updatedInvoice, Notification.NotificationType.INVOICE_SENT);
        
        return mapToResponseDto(updatedInvoice);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        
        if (!isInvoiceOwner(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        
        // Cannot delete invoices that are already paid or sent
        if (Invoice.Status.PAID.equals(invoice.getStatus()) || Invoice.Status.SENT.equals(invoice.getStatus())) {
            throw new IllegalStateException("Cannot delete a paid or sent invoice");
        }
        
        invoiceRepository.delete(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> getOverdueInvoices() {
        User currentUser = securityUtils.getCurrentUser();
        
        // Get all projects owned by the current freelancer
        List<Project> projects = projectRepository.findByFreelancer(currentUser, Pageable.unpaged())
                .getContent();
        
        // Get all invoices for these projects
        List<Invoice> allInvoices = invoiceRepository.findByProjectIn(projects);
        
        // Filter for overdue invoices (due date is in the past and status is not PAID or CANCELLED)
        LocalDate now = LocalDate.now();
        return allInvoices.stream()
                .filter(invoice -> invoice.getDueDate().isBefore(now) && 
                        !Invoice.Status.PAID.equals(invoice.getStatus()) && 
                        !Invoice.Status.CANCELLED.equals(invoice.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInvoiceOwner(Long id) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                log.error("isInvoiceOwner failed: Current user is null");
                return false;
            }

            Invoice invoice = invoiceRepository.findById(id).orElse(null);
            if (invoice == null) {
                log.warn("isInvoiceOwner check for non-existent invoice id: {}", id);
                return false;
            }

            // Check if freelancer relationship is properly loaded
            if (invoice.getFreelancer() == null) {
                log.error("isInvoiceOwner failed: Invoice {} has null freelancer", id);
                return false;
            }

            return invoice.getFreelancer().getId().equals(currentUser.getId());
        } catch (Exception e) {
            log.error("Exception in isInvoiceOwner for invoice ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public FileMetadata generateInvoicePdf(Long invoiceId) throws IOException {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        // Check if user has access to this invoice
        if (!isInvoiceOwner(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        
        // Check if PDF already exists for this invoice
        Optional<FileMetadata> existingPdf = fileMetadataRepository.findByEntityTypeAndEntityId("invoice", invoiceId)
                .stream()
                .findFirst();
        
        if (existingPdf.isPresent()) {
            // Delete existing PDF if it exists
            try {
                fileService.delete(existingPdf.get().getId());
            } catch (IOException e) {
                // Log error but continue with generating a new PDF
                log.error("Error deleting existing PDF: {}", e.getMessage());
            }
        }
        
        // Generate PDF content
        byte[] pdfContent = generatePdfContent(invoice);
        
        // Create a temporary file to upload
        String fileName = "Invoice_" + invoice.getInvoiceNumber() + ".pdf";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfContent);
        
        // Create a mock MultipartFile
        MockMultipartFile pdfFile = new MockMultipartFile(
                fileName,
                fileName,
                "application/pdf",
                pdfContent
        );
        
        // Upload the PDF file and associate it with the invoice
        return fileService.upload("invoice", invoiceId, pdfFile);
    }
    
    @Override
    public Resource downloadInvoicePdf(Long invoiceId) throws IOException {
        User currentUser = securityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
        
        // Check if user has access to this invoice
        if (!isInvoiceOwner(invoiceId) && 
            (invoice.getClient().getUser() == null || 
             !invoice.getClient().getUser().getId().equals(currentUser.getId()))) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        
        // Check if PDF exists for this invoice
        Optional<FileMetadata> existingPdf = fileMetadataRepository.findByEntityTypeAndEntityId("invoice", invoiceId)
                .stream()
                .findFirst();
        
        if (existingPdf.isPresent()) {
            // Return the existing PDF
            return fileService.download(existingPdf.get().getId());
        } else {
            // Generate a new PDF if it doesn't exist
            FileMetadata newPdf = generateInvoicePdf(invoiceId);
            return fileService.download(newPdf.getId());
        }
    }
    
    /**
     * Generates a unique invoice number with format INV-YYYY-MM-RANDOM
     */
    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = String.format("%d-%02d", now.getYear(), now.getMonthValue());
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + yearMonth + "-" + randomPart;
    }
    
    /**
     * Generate PDF content from an invoice
     */
    private byte[] generatePdfContent(Invoice invoice) throws IOException {
        // Use PdfBox for proper PDF generation
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Add invoice company logo/header
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            
            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("INVOICE");
            contentStream.endText();
            
            yPosition -= 30;
            
            // Invoice details section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Invoice Number: " + invoice.getInvoiceNumber());
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Status: " + invoice.getStatus());
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Issue Date: " + invoice.getIssueDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Due Date: " + invoice.getDueDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            contentStream.endText();

            // Spacing
            yPosition -= 40;
            
            // Client and company information
            float colWidth = tableWidth / 2;
            
            // "Bill To" section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Bill To:");
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(invoice.getClient().getName());
            contentStream.endText();
            
            if (invoice.getClient().getCompany() != null) {
                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(invoice.getClient().getCompany().getName());
                contentStream.endText();
            }
            
            if (invoice.getClient().getEmail() != null) {
                yPosition -= 15;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(invoice.getClient().getEmail());
                contentStream.endText();
            }
            
            // From section
            float rightColumnX = margin + colWidth;
            float fromSectionY = yPosition + 70; // Align with "Bill To" section
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(rightColumnX, fromSectionY);
            contentStream.showText("From:");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(rightColumnX, fromSectionY - 20);
            contentStream.showText(invoice.getFreelancer().getFirstName() + " " + invoice.getFreelancer().getLastName());
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(rightColumnX, fromSectionY - 35);
            contentStream.showText(invoice.getFreelancer().getEmail());
            contentStream.endText();
            
            // Project information if available
            if (invoice.getProject() != null) {
                yPosition -= 40;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Project: " + invoice.getProject().getName());
                contentStream.endText();
            } else {
                yPosition -= 40;
            }
            
            // Invoice items table
            yPosition -= 30;
            float rowHeight = 20f;
            float tableStartY = yPosition;
            
            // Draw table headers
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, tableStartY);
            contentStream.showText("Description");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + tableWidth * 0.5f, tableStartY);
            contentStream.showText("Quantity");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + tableWidth * 0.65f, tableStartY);
            contentStream.showText("Unit Price");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.newLineAtOffset(margin + tableWidth * 0.85f, tableStartY);
            contentStream.showText("Amount");
            contentStream.endText();
            
            // Draw horizontal line under headers
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, tableStartY - 5);
            contentStream.lineTo(margin + tableWidth, tableStartY - 5);
            contentStream.stroke();
            
            // Draw the invoice items
            yPosition = tableStartY - rowHeight;
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            
            for (InvoiceItem item : invoice.getItems()) {
                // Check if we need a new page
                if (yPosition < 100) {
                    contentStream.close();
                    PDPage newPage = new PDPage(PDRectangle.A4);
                    document.addPage(newPage);
                    contentStream = new PDPageContentStream(document, newPage);
                    yPosition = yStart - 50;
                }
                
                // Item description
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                
                // Limit description length to fit on page
                String description = item.getDescription();
                if (description.length() > 40) {
                    description = description.substring(0, 37) + "...";
                }
                contentStream.showText(description);
                contentStream.endText();
                
                // Item quantity
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + tableWidth * 0.5f, yPosition);
                contentStream.showText(item.getQuantity().toString());
                contentStream.endText();
                
                // Item unit price
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + tableWidth * 0.65f, yPosition);
                contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), item.getUnitPrice().doubleValue()));
                contentStream.endText();
                
                // Item amount
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + tableWidth * 0.85f, yPosition);
                contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), item.getAmount().doubleValue()));
                contentStream.endText();
                
                yPosition -= rowHeight;
            }
            
            // Draw horizontal line after items
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, yPosition - 5);
            contentStream.lineTo(margin + tableWidth, yPosition - 5);
            contentStream.stroke();
            
            // Invoice totals
            yPosition -= rowHeight;
            
            // Subtotal
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin + tableWidth * 0.65f, yPosition);
            contentStream.showText("Subtotal:");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin + tableWidth * 0.85f, yPosition);
            contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), invoice.getSubtotal().doubleValue()));
            contentStream.endText();
            
            // Tax
            yPosition -= rowHeight;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin + tableWidth * 0.65f, yPosition);
            contentStream.showText("Tax (" + invoice.getTaxRate() + "%):");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(margin + tableWidth * 0.85f, yPosition);
            contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), invoice.getTaxAmount().doubleValue()));
            contentStream.endText();
            
            // Total
            yPosition -= rowHeight;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin + tableWidth * 0.65f, yPosition);
            contentStream.showText("Total:");
            contentStream.endText();
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin + tableWidth * 0.85f, yPosition);
            contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), invoice.getAmount().doubleValue()));
            contentStream.endText();
            
            // Amount paid
            if (invoice.getAmountPaid() != null && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
                yPosition -= rowHeight;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin + tableWidth * 0.65f, yPosition);
                contentStream.showText("Amount Paid:");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin + tableWidth * 0.85f, yPosition);
                contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), invoice.getAmountPaid().doubleValue()));
                contentStream.endText();
                
                // Balance due
                yPosition -= rowHeight;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin + tableWidth * 0.65f, yPosition);
                contentStream.showText("Balance Due:");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin + tableWidth * 0.85f, yPosition);
                contentStream.showText(String.format("%s %.2f", invoice.getCurrency(), invoice.getAmountDue().doubleValue()));
                contentStream.endText();
            }
            
            // Notes
            if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
                yPosition -= rowHeight * 2;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Notes:");
                contentStream.endText();
                
                yPosition -= rowHeight;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                
                // Limit notes length and handle potential multi-line display
                String notes = invoice.getNotes();
                if (notes.length() > 100) {
                    contentStream.showText(notes.substring(0, 97) + "...");
                } else {
                    contentStream.showText(notes);
                }
                contentStream.endText();
            }
            
            // Payment information
            if (invoice.getPaymentMethod() != null && !invoice.getPaymentMethod().isEmpty()) {
                yPosition -= rowHeight * 2;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Payment Method:");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin + 120, yPosition);
                contentStream.showText(invoice.getPaymentMethod());
                contentStream.endText();
            }
            
            // Footer with thank you message
            yPosition = 50;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD_OBLIQUE, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Thank you for your business!");
            contentStream.endText();
            
            contentStream.close();
            
            // Save the document to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void calculateInvoiceTotals(Invoice invoice) {
        // Calculate subtotal
        BigDecimal subtotal = invoice.getItems().stream()
                .map(item -> {
                    // Calculate and set item amount
                    BigDecimal amount = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity().toString()));
                    item.setAmount(amount);
                    return amount;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        invoice.setSubtotal(subtotal);
        
        // Calculate tax amount
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (invoice.getTaxRate() != null && invoice.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
            taxAmount = subtotal.multiply(invoice.getTaxRate()).divide(BigDecimal.valueOf(100));
        }
        invoice.setTaxAmount(taxAmount);
        
        // Calculate total - set directly to amount field instead of calling setTotal
        BigDecimal total = subtotal.add(taxAmount);
        invoice.setAmount(total);
        
        // Initialize paid amount if not set
        if (invoice.getAmountPaid() == null) {
            invoice.setAmountPaid(BigDecimal.ZERO);
        }
        
        // Calculate amount due
        invoice.setAmountDue(total.subtract(invoice.getAmountPaid()));
    }
    
    private InvoiceResponseDto mapToResponseDto(Invoice invoice) {
        // Explicitly fetch invoice items from repository to ensure they're loaded
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        
        List<InvoiceItemResponseDto> itemDtos = items.stream().map(item -> 
            InvoiceItemResponseDto.builder()
                .id(item.getId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .projectId(invoice.getProject() != null ? invoice.getProject().getId() : null)
                .build()
        ).collect(Collectors.toList());

        // Check for client information
        String clientName = invoice.getClient() != null ? invoice.getClient().getName() : null;
        // Get company name from client's company if available
        String clientCompanyName = null;
        if (invoice.getClient() != null && invoice.getClient().getCompany() != null) {
            clientCompanyName = invoice.getClient().getCompany().getName();
        }
        
        // Check for project information
        Long projectId = invoice.getProject() != null ? invoice.getProject().getId() : null;
        String projectName = invoice.getProject() != null ? invoice.getProject().getName() : null;
        
        // Calculate if invoice is overdue
        boolean isOverdue = false;
        if (invoice.getDueDate() != null && 
            !Invoice.Status.PAID.equals(invoice.getStatus()) && 
            !Invoice.Status.CANCELLED.equals(invoice.getStatus())) {
            isOverdue = invoice.getDueDate().isBefore(LocalDate.now());
        }
        
        return InvoiceResponseDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .clientId(invoice.getClient() != null ? invoice.getClient().getId() : null)
                .clientName(clientName)
                .clientCompanyName(clientCompanyName)
                .projectId(projectId)
                .projectName(projectName)
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .items(itemDtos)
                .subtotal(invoice.getSubtotal() != null ? invoice.getSubtotal() : BigDecimal.ZERO)
                .taxAmount(invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO)
                .taxRate(invoice.getTaxRate() != null ? invoice.getTaxRate() : BigDecimal.ZERO)
                .discount(invoice.getDiscount())
                .total(invoice.getAmount() != null ? invoice.getAmount() : BigDecimal.ZERO) 
                .amountPaid(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO)
                .amountDue(invoice.getAmountDue() != null ? invoice.getAmountDue() : BigDecimal.ZERO)
                .notes(invoice.getNotes())
                .currency(invoice.getCurrency())
                .paidDate(invoice.getPaidDate())
                .sentDate(invoice.getSentDate())
                .isOverdue(isOverdue)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
    
    private InvoiceDto mapToDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .projectId(invoice.getProject() != null ? invoice.getProject().getId() : null)
                .clientId(invoice.getClient().getId())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())  // Changed from paymentDate to paidDate
                .description(invoice.getNotes())
                .paymentMethod(invoice.getPaymentMethod())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
    
    private Boolean isInvoiceOverdue(Invoice invoice) {
        if (Invoice.Status.PAID.equals(invoice.getStatus()) || Invoice.Status.CANCELLED.equals(invoice.getStatus())) {
            return false;
        }
        
        return invoice.getDueDate().isBefore(LocalDate.now());
    }
    
    // Helper class for creating a MultipartFile from a byte array
    private static class MockMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public MockMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public ByteArrayInputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getInvoicePayments(Long invoiceId) {
        // Delegate to the PaymentService which already has all the necessary business logic
        return paymentService.getPaymentsByInvoice(invoiceId);
    }
}