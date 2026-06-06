package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;

public record UpdateStudentRequestDto(
    String firstName,
    String lastName,
    String phone,
    String course,
    @Min(value = 1, message = "Class must be at least 1")
    @Max(value = 14, message = "Class must be at most 14")
    Integer currentClass,
    String rollNumber,
    String parentName,
    String parentPhone,
    @Email(message = "Parent email must be a valid email")
    String parentEmail,
    String address,
    LocalDateTime dateOfBirth,
    String bloodGroup,
    String emergencyContact,
    Long instituteId,
    Boolean enabled
) {}
