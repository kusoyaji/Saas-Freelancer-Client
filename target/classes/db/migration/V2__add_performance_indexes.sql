-- Add indexes to improve query performance

-- Project-related indexes
CREATE INDEX idx_project_freelancer ON projects(freelancer_id);
CREATE INDEX idx_project_client ON projects(client_id);

-- Invoice-related indexes
CREATE INDEX idx_invoice_project ON invoices(project_id);
CREATE INDEX idx_invoice_client ON invoices(client_id);
CREATE INDEX idx_invoice_freelancer ON invoices(freelancer_id);
CREATE INDEX idx_invoice_status ON invoices(status);
CREATE INDEX idx_invoice_due_date ON invoices(due_date);
CREATE INDEX idx_invoice_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoice_number ON invoices(invoice_number);

-- InvoiceItem-related indexes
CREATE INDEX idx_invoice_item_invoice ON invoice_items(invoice_id);

-- Payment-related indexes
CREATE INDEX idx_payment_invoice ON payments(invoice_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_date ON payments(payment_date);

-- TimeEntry-related indexes
CREATE INDEX idx_time_entry_project ON time_entries(project_id);
CREATE INDEX idx_time_entry_user ON time_entries(user_id);
CREATE INDEX idx_time_entry_date ON time_entries(start_time);
CREATE INDEX idx_time_entry_billing ON time_entries(billable, billed);

-- User-related indexes
CREATE INDEX idx_user_email ON users(email);

-- Client-related indexes
CREATE INDEX idx_client_freelancer ON clients(freelancer_id);

-- Composite indexes for frequently combined filters
CREATE INDEX idx_invoice_freelancer_status ON invoices(freelancer_id, status);
CREATE INDEX idx_invoice_client_status ON invoices(client_id, status);
CREATE INDEX idx_invoice_project_status ON invoices(project_id, status);
CREATE INDEX idx_time_entry_project_billable ON time_entries(project_id, billable, billed);