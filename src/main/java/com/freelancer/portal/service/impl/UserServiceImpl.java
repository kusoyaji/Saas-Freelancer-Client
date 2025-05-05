package com.freelancer.portal.service.impl;

import com.freelancer.portal.dto.UserDTO;
import com.freelancer.portal.dto.UserUpdateDto;
import com.freelancer.portal.mapper.UserMapper;
import com.freelancer.portal.model.User;
import com.freelancer.portal.repository.UserRepository;
import com.freelancer.portal.security.SecurityUtils;
import com.freelancer.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    public UserDTO getCurrentUser() {
        User user = securityUtils.getCurrentUser();
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO updateCurrentUser(UserUpdateDto userUpdateDto) {
        User currentUser = securityUtils.getCurrentUser();
        return updateUser(currentUser.getId(), userUpdateDto);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        // Handle email specially (check for duplicates and reset verification)
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.existsByEmail(userUpdateDto.getEmail())) {
                throw new IllegalStateException("Email already in use: " + userUpdateDto.getEmail());
            }
            user.setEmail(userUpdateDto.getEmail());
            // Reset email verification when email is changed
            user.setEmailVerified(false);
        }

        // Use the mapper to update all other user fields from DTO
        user = UserMapper.updateFromDto(user, userUpdateDto);

        // Update additional fields that might not be in the mapper
        if (userUpdateDto.getPhone() != null) {
            user.setPhone(userUpdateDto.getPhone());
        }
        if (userUpdateDto.getBio() != null) {
            user.setBio(userUpdateDto.getBio());
        }
        if (userUpdateDto.getWebsite() != null) {
            user.setWebsite(userUpdateDto.getWebsite());
        }
        if (userUpdateDto.getCompany() != null) {
            user.setCompany(userUpdateDto.getCompany());
        }
        if (userUpdateDto.getPosition() != null) {
            user.setPosition(userUpdateDto.getPosition());
        }
        if (userUpdateDto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(userUpdateDto.getProfilePictureUrl());
        }

        // Password changes should be handled by a separate service method with proper security

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public User saveUser(User user) {
        log.debug("Saving user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        log.debug("Updating user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserEntityById(Long id) {
        log.debug("Getting user entity by id: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination");
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists with email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDTO getUserDTOByEmail(String email) {
        log.debug("Getting user DTO by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserDTOById(Long id) {
        log.debug("Getting user DTO by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateUserDTO(Long id, UserDTO userDTO) {
        log.debug("Updating user DTO with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        // Update only allowed fields
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhone(userDTO.getPhone());
        user.setBio(userDTO.getBio());
        user.setWebsite(userDTO.getWebsite());
        user.setCompany(userDTO.getCompany());
        user.setPosition(userDTO.getPosition());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        
        // Email and role changes are typically handled separately with additional security
        
        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }
}