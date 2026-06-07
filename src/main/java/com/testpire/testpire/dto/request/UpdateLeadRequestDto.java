package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.Board;
import com.testpire.testpire.enums.Gender;
import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
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

    LeadStatus status,

    LeadSource source,

    Long interestedCourseId,

    String assignedTo,

    LocalDate nextFollowUpDate,

    String notes
) {}
