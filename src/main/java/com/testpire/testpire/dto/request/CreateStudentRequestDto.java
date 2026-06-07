package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;
import java.util.List;

public record CreateStudentRequestDto(
    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    String username,
    
    @NotBlank(message = "First name is required")
    String firstName,
    
    @NotBlank(message = "Last name is required")
    String lastName,
    
    @NotNull(message = "Institute ID is required")
    Long instituteId,
    
    String phone,
    
    String course,

    @Min(value = 1, message = "Class must be at least 1")
    @Max(value = 14, message = "Class must be at most 14")
    Integer currentClass,

    Gender gender,

    String rollNumber,
    
    String parentName,
    
    String parentPhone,
    
    @Email(message = "Parent email must be a valid email")
    String parentEmail,
    
    String address,
    
    LocalDate dateOfBirth,
    
    String bloodGroup,

    String emergencyContact,

    /** Optional initial course+batch assignments. Each batch must belong to its course and institute. */
    @Valid
    List<EnrollmentRequestDto> enrollments
) {}
