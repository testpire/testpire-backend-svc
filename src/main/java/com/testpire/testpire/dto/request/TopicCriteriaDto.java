package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TopicCriteriaDto {
    
    private Long instituteId;
    private Long courseId;
    private Long subjectId;
    private Long chapterId;
    private String searchText;
    private String name;
    private String code;
    private String description;
    private String content;
    private String learningOutcomes;
    private Integer minOrderIndex;
    private Integer maxOrderIndex;
    private Integer minDuration;
    private Integer maxDuration;
    private Boolean active;
    private Boolean hasQuestions;
    private Integer minQuestions;
    private Integer maxQuestions;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
