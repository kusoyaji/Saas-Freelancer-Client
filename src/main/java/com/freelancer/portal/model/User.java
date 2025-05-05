package com.freelancer.portal.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a user in the freelancing platform.
 * <p>
 * Users can be either freelancers who manage clients and projects, or clients who 
 * can access and interact with specific projects. The class implements Spring Security's
 * UserDetails interface to integrate with the authentication system.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode(exclude = {"conversations"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user's first name.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * The user's last name.
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * The user's email address, used as the username for authentication.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The user's hashed password.
     */
    @JsonIgnore
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    private String phone;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    /**
     * The user's role in the system (FREELANCER or CLIENT).
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String website;

    private String company;

    private String position;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "account_locked")
    private Boolean accountLocked;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "freelancer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Project> projects;

    /**
     * List of clients associated with this user (when the user is a freelancer).
     */
    @JsonManagedReference("user-clients")
    @OneToMany(mappedBy = "freelancer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Client> clients;
    
    /**
     * Conversations that this user participates in.
     */
    @JsonBackReference
    @ManyToMany(mappedBy = "participants")
    private Set<Conversation> conversations;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (emailVerified == null) {
            emailVerified = false;
        }
        if (accountLocked == null) {
            accountLocked = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Possible roles for a user in the system.
     */
    public enum Role {
        USER, FREELANCER, CLIENT, ADMIN
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return accountLocked == null || !accountLocked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        // Allow login regardless of email verification status
        // You can implement access control for specific features based on emailVerified elsewhere
        return true;
    }

    // Custom hashCode implementation to avoid circular references
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    // Custom equals implementation to avoid circular references
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }
}