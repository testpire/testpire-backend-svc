package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentCriteriaDto {
    
    private Long instituteId;
    private String searchText;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String course;
    private Integer minYearOfStudy;
    private Integer maxYearOfStudy;
    private String rollNumber;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private String address;
    private String bloodGroup;
    private String emergencyContact;
    private Boolean enabled;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
