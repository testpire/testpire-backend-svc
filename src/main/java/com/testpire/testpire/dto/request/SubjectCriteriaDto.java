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
    private Boolean hasChapters;
    private Integer minChapters;
    private Integer maxChapters;
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
}
