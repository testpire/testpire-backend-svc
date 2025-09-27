package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record CreateTeacherRequestDto(
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    String username,
    
    @NotBlank(message = "Password is required")
    String password,
    
    @NotBlank(message = "First name is required")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    String lastName,
    
    Long instituteId,
    
    String phone,
    
    String department,
    
    String subject,
    
    String qualification,
    
    Integer experienceYears,
    
    String specialization,
    
    String bio
) {}
