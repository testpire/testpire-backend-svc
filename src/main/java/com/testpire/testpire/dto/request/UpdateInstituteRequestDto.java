package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateInstituteRequestDto(
    String name,
    
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
    
    String postalCode,
    
    Boolean active
) {}
