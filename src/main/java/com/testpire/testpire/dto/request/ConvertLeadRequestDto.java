package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Enrolls a lead: provisions a Cognito user + {@code User} + {@code StudentDetails} and links them
 * back to the lead. firstName/lastName/phone are taken from the lead; the fields here are the
 * student-specific data plus the course being enrolled into.
 *
 * <p>{@code email} is the login username (Cognito emails the temp password to it). If null, the
 * lead's own email is used; one of the two must be a valid email.</p>
 */
public record ConvertLeadRequestDto(
    @NotNull(message = "Enrolled course ID is required")
    Long enrolledCourseId,

    @Email(message = "Email must be valid")
    String email,

    String rollNumber,

    String parentName,

    String parentPhone,

    @Email(message = "Parent email must be valid")
    String parentEmail,

    String address,

    LocalDateTime dateOfBirth,

    String bloodGroup,

    String emergencyContact
) {}
