package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record CreateUserRequestDto(
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    String username,
    
    @NotBlank(message = "Password is required")
    String password,
    
    @NotBlank(message = "First name is required")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    String lastName,
    
    UserRole role,
    
    Long instituteId
) {}
