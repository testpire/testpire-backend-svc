package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseCriteriaDto {
    
    private Long instituteId;
    private String searchText;
    private String name;
    private String code;
    private String description;
    private Integer minDuration;
    private Integer maxDuration;
    private String level;
    private String prerequisites;
    private Boolean active;
    private Boolean hasSubjects;
    private Integer minSubjects;
    private Integer maxSubjects;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
