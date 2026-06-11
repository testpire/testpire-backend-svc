package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.List;

public record UpdateStudentRequestDto(
    String firstName,
    String lastName,
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
    Long instituteId,

    /**
     * When non-null, REPLACES the student's full enrollment set to match this list (an empty list
     * clears all enrollments). When null, enrollments are left unchanged.
     */
    @Valid
    List<EnrollmentRequestDto> enrollments
) {}
