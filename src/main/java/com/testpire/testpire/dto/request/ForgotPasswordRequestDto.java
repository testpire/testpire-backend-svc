package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
    @NotBlank(message = "Username is required")
    String username
) {}
