package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record LoginRequestDto(
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    String username,
    
    @NotBlank(message = "Password is required")
    String password
) {}
