package com.testpire.testpire.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
