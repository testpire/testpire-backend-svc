package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Enrolls a lead: provisions a Cognito user + {@code User} + {@code StudentDetails} and links them
 * back to the lead. firstName/lastName/phone are taken from the lead; the fields here are the
 * student-specific data plus the course being enrolled into.
 *
 * <p>{@code email} is the login username (Cognito emails the temp password to it). If null, the
 * lead's own email is used; one of the two must be a valid email.</p>
 *
 * <p>Parent contact, gender and current class are NOT taken here — they are captured on the lead
 * at enquiry time and carried into {@code StudentDetails} during conversion.</p>
 */
public record ConvertLeadRequestDto(
    @NotNull(message = "Enrolled course ID is required")
    Long enrolledCourseId,

    /** Optional batch within the enrolled course; when provided, an enrollment row is created. */
    Long enrolledBatchId,

    @Email(message = "Email must be valid")
    String email,

    String rollNumber,

    String address,

    LocalDate dateOfBirth,

    String bloodGroup,

    String emergencyContact
) {}
