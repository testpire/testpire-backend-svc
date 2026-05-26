package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequestDto(
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    String username,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotNull(message = "Role is required")
    UserRole role,

    @NotNull(message = "Institute ID is required")
    Long instituteId
) {}
