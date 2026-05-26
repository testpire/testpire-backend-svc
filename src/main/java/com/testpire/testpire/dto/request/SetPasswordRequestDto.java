package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequestDto(
    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Session is required")
    String session,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword
) {}
