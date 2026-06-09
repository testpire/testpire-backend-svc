package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Lead;
import com.testpire.testpire.enums.Board;
import com.testpire.testpire.enums.Gender;
import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

public record LeadResponseDto(
    Long id,
    Long instituteId,
    String firstName,
    String lastName,
    String email,
    String phone,
    Gender gender,
    String school,
    Integer currentClass,
    Board board,
    BigDecimal courseFeeCommitted,
    String parentName,
    String parentPhone,
    String parentEmail,
    LeadStatus status,
    LeadSource source,
    Long interestedCourseId,
    String assignedTo,
    LocalDate nextFollowUpDate,
    String notes,
    Long convertedUserId,
    Long enrolledCourseId,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {
    public static LeadResponseDto fromEntity(Lead lead) {
        return new LeadResponseDto(
            lead.getId(),
            lead.getInstituteId(),
            lead.getFirstName(),
            lead.getLastName(),
            lead.getEmail(),
            lead.getPhone(),
            lead.getGender(),
            lead.getSchool(),
            lead.getCurrentClass(),
            lead.getBoard(),
            lead.getCourseFeeCommitted(),
            lead.getParentName(),
            lead.getParentPhone(),
            lead.getParentEmail(),
            lead.getStatus(),
            lead.getSource(),
            lead.getInterestedCourseId(),
            lead.getAssignedTo(),
            lead.getNextFollowUpDate(),
            lead.getNotes(),
            lead.getConvertedUserId(),
            lead.getEnrolledCourseId(),
            lead.getCreatedAt(),
            lead.getUpdatedAt(),
            lead.getCreatedBy(),
            lead.getUpdatedBy()
        );
    }
}
