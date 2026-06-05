package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadCriteriaDto {

    private Long instituteId;
    private String searchText;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LeadStatus status;
    private LeadSource source;
    private Long interestedCourseId;
    private String assignedTo;
    private Boolean converted;
    private LocalDate followUpFrom;
    private LocalDate followUpTo;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
