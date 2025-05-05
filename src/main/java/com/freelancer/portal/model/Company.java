package com.freelancer.portal.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a company or business entity in the freelancing platform.
 * <p>
 * Companies represent the business entities of freelancers, enabling them to maintain
 * a professional identity separate from their personal details. Companies have their own
 * contact information, branding elements, and can be associated with clients and projects.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The registered legal name of the company.
     */
    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    @Column(nullable = false)
    private String name;

    /**
     * A brief description of the company's services or mission.
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The company's official email address for business communications.
     */
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    /**
     * The company's phone number.
     */
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phone;

    /**
     * The company's website URL.
     */
    @Size(max = 100, message = "Website cannot exceed 100 characters")
    private String website;

    /**
     * The company's physical address.
     */
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    /**
     * The company's city.
     */
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    /**
     * The company's state or province.
     */
    @Size(max = 50, message = "State cannot exceed 50 characters")
    private String state;

    /**
     * The company's postal code.
     */
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    @Column(name = "postal_code")
    private String postalCode;

    /**
     * The company's country.
     */
    @Size(max = 50, message = "Country cannot exceed 50 characters")
    private String country;

    /**
     * The company's tax identification number.
     */
    @Size(max = 30, message = "Tax ID cannot exceed 30 characters")
    @Column(name = "tax_id")
    private String taxId;

    /**
     * URL to the company's logo image.
     */
    @Column(name = "logo_url")
    private String logoUrl;

    /**
     * URL to the company's banner image.
     */
    @Size(max = 1000, message = "Banner URL cannot exceed 1000 characters")
    @Column(name = "banner_url")
    private String bannerUrl;

    /**
     * Primary color for the company's branding (hex code).
     */
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Primary color must be a valid hex color code")
    @Column(name = "primary_color")
    private String primaryColor;

    /**
     * Secondary color for the company's branding (hex code).
     */
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Secondary color must be a valid hex color code")
    @Column(name = "secondary_color")
    private String secondaryColor;

    /**
     * The company's slogan or tagline.
     */
    @Size(max = 200, message = "Slogan cannot exceed 200 characters")
    private String slogan;

    /**
     * Clients associated with this company.
     */
    @JsonManagedReference("company-clients")
    @OneToMany(mappedBy = "company")
    private List<Client> clients;

    /**
     * Timestamp when the company record was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the company record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}