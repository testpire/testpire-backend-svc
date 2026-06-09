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
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
}
