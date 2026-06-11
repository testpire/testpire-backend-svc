package com.testpire.testpire.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCriteriaDto {
    
    private Long instituteId;
    private String searchText;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String department;
    private String subject;
    private String qualification;
    private Integer minExperienceYears;
    private Integer maxExperienceYears;
    private String specialization;
    private String bio;
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
}
