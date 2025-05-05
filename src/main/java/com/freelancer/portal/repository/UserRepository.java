package com.freelancer.portal.repository;

import com.freelancer.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by their email address.
     * 
     * @param email the email address
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find a user by their email address, ignoring case sensitivity.
     * 
     * @param email the email address
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmailIgnoreCase(String email);
    
    /**
     * Check if a user exists with the given email address.
     * 
     * @param email the email address
     * @return true if a user exists with the given email, false otherwise
     */
    boolean existsByEmail(String email);
}