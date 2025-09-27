package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;

public record CreateStudentRequestDto(
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    String username,
    
    @NotBlank(message = "Password is required")
    String password,
    
    @NotBlank(message = "First name is required")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    String lastName,
    
    @NotNull(message = "Institute ID is required")
    Long instituteId,
    
    String phone,
    
    String course,
    
    @Min(value = 1, message = "Year of study must be at least 1")
    @Max(value = 10, message = "Year of study must be at most 10")
    Integer yearOfStudy,
    
    String rollNumber,
    
    String parentName,
    
    String parentPhone,
    
    @Email(message = "Parent email must be a valid email")
    String parentEmail,
    
    String address,
    
    LocalDateTime dateOfBirth,
    
    String bloodGroup,
    
    String emergencyContact
) {}
