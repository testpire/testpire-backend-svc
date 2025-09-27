package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
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
    private Boolean enabled;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
