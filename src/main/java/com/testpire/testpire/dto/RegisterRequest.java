package com.testpire.testpire.dto;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = ApplicationConstants.Messages.USERNAME_REQUIRED)
    @Size(min = ApplicationConstants.Validation.USERNAME_MIN_LENGTH, max = ApplicationConstants.Validation.USERNAME_MAX_LENGTH, message = "Username must be between " + ApplicationConstants.Validation.USERNAME_MIN_LENGTH + " and " + ApplicationConstants.Validation.USERNAME_MAX_LENGTH + " characters")
    String username,

    @Email(message = "Email must be valid")
    @NotBlank(message = ApplicationConstants.Messages.EMAIL_REQUIRED)
    String email,

    @NotBlank(message = ApplicationConstants.Messages.PASSWORD_REQUIRED)
    @Size(min = ApplicationConstants.Validation.PASSWORD_MIN_LENGTH, message = "Password must be at least " + ApplicationConstants.Validation.PASSWORD_MIN_LENGTH + " characters long")
    String password,

    @NotBlank(message = ApplicationConstants.Messages.FIRST_NAME_REQUIRED)
    @Size(max = ApplicationConstants.Validation.FIRST_NAME_MAX_LENGTH, message = "First name must not exceed " + ApplicationConstants.Validation.FIRST_NAME_MAX_LENGTH + " characters")
    String firstName,

    @NotBlank(message = ApplicationConstants.Messages.LAST_NAME_REQUIRED)
    @Size(max = ApplicationConstants.Validation.LAST_NAME_MAX_LENGTH, message = "Last name must not exceed " + ApplicationConstants.Validation.LAST_NAME_MAX_LENGTH + " characters")
    String lastName,

    @NotNull(message = ApplicationConstants.Messages.USER_ROLE_REQUIRED)
    UserRole role,

    @NotBlank(message = ApplicationConstants.Messages.INSTITUTE_ID_REQUIRED)
    String instituteId
) {}