package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

/**
 * Partial update of a lead's pipeline state and follow-up fields. All fields optional; null = leave unchanged.
 *
 * <p>{@code status} may move the lead through the pipeline, but setting it to {@link LeadStatus#ENROLLED}
 * here is rejected by the service — enrollment must go through the convert endpoint so a real student
 * account is provisioned and linked.</p>
 */
public record UpdateLeadRequestDto(
    String firstName,

    String lastName,

    @Email(message = "Email must be valid")
    String email,

    String phone,

    LeadStatus status,

    LeadSource source,

    Long interestedCourseId,

    String assignedTo,

    LocalDate nextFollowUpDate,

    String notes
) {}
