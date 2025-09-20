package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record CreateInstituteRequestDto(
    @NotBlank(message = "Institute name is required")
    String name,
    
    @NotBlank(message = "Institute code is required")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Institute code must be 2-10 characters, uppercase letters and numbers only")
    String code,
    
    String description,
    
    @Email(message = "Email must be valid")
    String email,
    
    String phone,
    
    String website,
    
    String address,
    
    String city,
    
    String state,
    
    String country,
    
    String postalCode
) {}
