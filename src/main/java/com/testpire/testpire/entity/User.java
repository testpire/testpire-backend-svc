package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = ApplicationConstants.Database.USERS_TABLE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = ApplicationConstants.Messages.USERNAME_REQUIRED)
    @Size(min = ApplicationConstants.Validation.USERNAME_MIN_LENGTH, max = ApplicationConstants.Validation.USERNAME_MAX_LENGTH, message = "Username must be between " + ApplicationConstants.Validation.USERNAME_MIN_LENGTH + " and " + ApplicationConstants.Validation.USERNAME_MAX_LENGTH + " characters")
    private String username;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Email must be valid")
    @NotBlank(message = ApplicationConstants.Messages.EMAIL_REQUIRED)
    private String email;
    
    @Column(nullable = false)
    @NotBlank(message = ApplicationConstants.Messages.FIRST_NAME_REQUIRED)
    @Size(max = ApplicationConstants.Validation.FIRST_NAME_MAX_LENGTH, message = "First name must not exceed " + ApplicationConstants.Validation.FIRST_NAME_MAX_LENGTH + " characters")
    private String firstName;
    
    @Column(nullable = false)
    @NotBlank(message = ApplicationConstants.Messages.LAST_NAME_REQUIRED)
    @Size(max = ApplicationConstants.Validation.LAST_NAME_MAX_LENGTH, message = "Last name must not exceed " + ApplicationConstants.Validation.LAST_NAME_MAX_LENGTH + " characters")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = ApplicationConstants.Messages.USER_ROLE_REQUIRED)
    private UserRole role;
    
    @Column(name = ApplicationConstants.Database.INSTITUTE_ID_COLUMN, nullable = false)
    @NotBlank(message = ApplicationConstants.Messages.INSTITUTE_ID_REQUIRED)
    private String instituteId;
    
    @Column(name = ApplicationConstants.Database.COGNITO_USER_ID_COLUMN)
    private String cognitoUserId;
    
    @Builder.Default
    private boolean enabled = true;
    
    @Builder.Default
    @Column(name = ApplicationConstants.Database.CREATED_AT_COLUMN)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(name = ApplicationConstants.Database.UPDATED_AT_COLUMN)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = ApplicationConstants.Database.CREATED_BY_COLUMN)
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 