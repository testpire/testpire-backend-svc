package com.testpire.testpire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record InstituteDto(
    @NotBlank(message = "Institute name is required")
    @Size(min = 2, max = 100, message = "Institute name must be between 2 and 100 characters")
    String name,
    
    @NotBlank(message = "Institute code is required")
    @Size(min = 2, max = 20, message = "Institute code must be between 2 and 20 characters")
    String code,
    
    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    String state,
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country,
    
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    String postalCode,
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String phone,
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    String email,
    
    String website,
    
    String description
) {} 