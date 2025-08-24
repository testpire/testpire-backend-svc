package com.testpire.testpire.dto;

import jakarta.validation.constraints.NotBlank;

// LoginRequest.java
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}