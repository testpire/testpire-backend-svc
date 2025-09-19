package com.testpire.testpire.service;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final InstituteService instituteService;

    public User createUser(RegisterRequest request, UserRole role, String cognitoUserId, String createdBy) {
        log.info("Creating user: {} with role: {}", request.username(), role);
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(String.format(ApplicationConstants.Messages.USER_ALREADY_EXISTS, request.username()));
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(String.format(ApplicationConstants.Messages.USER_EMAIL_EXISTS, request.email()));
        }

        // Validate institute exists
        if (!instituteService.instituteExistsById(request.instituteId())) {
            throw new IllegalArgumentException(ApplicationConstants.Messages.INSTITUTE_NOT_FOUND + request.instituteId());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(role)
                .instituteId(request.instituteId())
                .cognitoUserId(cognitoUserId)
                .enabled(true)
                .createdBy(createdBy)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public User updateUser(Long id, RegisterRequest request, String updatedBy) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Check if username is being changed and if new username already exists
        if (!existingUser.getUsername().equals(request.username()) && 
            userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("User with username " + request.username() + " already exists");
        }

        // Check if email is being changed and if new email already exists
        if (!existingUser.getEmail().equals(request.email()) && 
            userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        // Validate institute exists
        if (!instituteService.instituteExistsById(request.instituteId())) {
            throw new IllegalArgumentException("Institute not found with ID: " + request.instituteId());
        }

        existingUser.setUsername(request.username());
        existingUser.setEmail(request.email());
        existingUser.setFirstName(request.firstName());
        existingUser.setLastName(request.lastName());
        existingUser.setInstituteId(request.instituteId());
        existingUser.setUpdatedBy(updatedBy);

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User deactivated successfully with ID: {}", id);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    public User getUserByCognitoUserId(String cognitoUserId) {
        return userRepository.findByCognitoUserId(cognitoUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with cognitoUserId: " + cognitoUserId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndEnabledTrue(role);
    }

    public List<User> getUsersByInstitute(Long instituteId) {
        return userRepository.findByInstituteIdAndEnabledTrue(instituteId);
    }

    public List<User> getUsersByRoleAndInstitute(UserRole role, Long instituteId) {
        return userRepository.findByRoleAndInstituteIdAndEnabledTrue(role, instituteId);
    }

    public List<User> searchUsersByRoleAndInstitute(UserRole role, Long instituteId, String searchTerm) {
        if (instituteId == null) {
            // For SUPER_ADMIN - search across all institutes
            return userRepository.findByRoleAndEnabledTrueAndNameOrEmailContaining(role, searchTerm);
        } else {
            // For INST_ADMIN and TEACHER - search within their institute
            return userRepository.findByRoleAndInstituteIdAndEnabledTrueAndNameOrEmailContaining(role, instituteId, searchTerm);
        }
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByEnabledTrue();
    }

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean userExistsById(Long id) {
        return userRepository.existsById(id);
    }
} 