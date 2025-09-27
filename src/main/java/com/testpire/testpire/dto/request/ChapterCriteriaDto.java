package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChapterCriteriaDto {
    
    private Long instituteId;
    private Long subjectId;
    private String searchText;
    private String name;
    private String code;
    private String description;
    private Integer minOrderIndex;
    private Integer maxOrderIndex;
    private Integer minDuration;
    private Integer maxDuration;
    private String objectives;
    private Boolean active;
    private Boolean hasTopics;
    private Integer minTopics;
    private Integer maxTopics;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
