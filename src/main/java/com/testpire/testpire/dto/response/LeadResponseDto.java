package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Lead;
import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeadResponseDto(
    Long id,
    Long instituteId,
    String firstName,
    String lastName,
    String email,
    String phone,
    LeadStatus status,
    LeadSource source,
    Long interestedCourseId,
    String assignedTo,
    LocalDate nextFollowUpDate,
    String notes,
    Long convertedUserId,
    Long enrolledCourseId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
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
