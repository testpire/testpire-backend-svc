package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuestionCriteriaDto {
    
    private Long instituteId;
    private Long courseId;
    private Long subjectId;
    private Long chapterId;
    private Long topicId;
    private String searchText;
    private DifficultyLevel difficultyLevel;
    private String questionType;
    private Integer minMarks;
    private Integer maxMarks;
    private Integer minNegativeMarks;
    private Integer maxNegativeMarks;
    private Boolean hasQuestionImage;
    private Boolean hasExplanation;
    private Boolean hasCorrectOption;
    private Boolean hasOptions;
    private Integer minOptions;
    private Integer maxOptions;
    private Boolean active;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
}
