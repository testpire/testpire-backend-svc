package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubjectCriteriaDto {
    
    private Long instituteId;
    private Long courseId;
    private String searchText;
    private String name;
    private String code;
    private String description;
    private Integer minDuration;
    private Integer maxDuration;
    private Integer minCredits;
    private Integer maxCredits;
    private String prerequisites;
    private Boolean active;
    private Boolean hasChapters;
    private Integer minChapters;
    private Integer maxChapters;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
