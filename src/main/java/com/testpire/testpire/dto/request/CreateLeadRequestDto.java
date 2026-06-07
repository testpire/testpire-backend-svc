package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.Board;
import com.testpire.testpire.enums.Gender;
import com.testpire.testpire.enums.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Captures a new enquiry. No login account is created — see the convert endpoint for that.
 * {@code instituteId} is honored only for SUPER_ADMIN; other callers are scoped to their JWT institute.
 */
public record CreateLeadRequestDto(
    @NotBlank(message = "First name is required")
    String firstName,

    String lastName,

    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Phone is required")
    String phone,

    Gender gender,

    String school,

    @Min(value = 1, message = "Class must be at least 1")
    @Max(value = 14, message = "Class must be at most 14")
    Integer currentClass,

    Board board,

    BigDecimal courseFeeCommitted,

    String parentName,

    String parentPhone,

    @Email(message = "Parent email must be valid")
    String parentEmail,

    LeadSource source,

    Long interestedCourseId,

    String assignedTo,

    LocalDate nextFollowUpDate,

    String notes,

    Long instituteId
) {}
