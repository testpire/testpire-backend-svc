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
    private Boolean hasQuestions;
    private Integer minQuestions;
    private Integer maxQuestions;
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
}
