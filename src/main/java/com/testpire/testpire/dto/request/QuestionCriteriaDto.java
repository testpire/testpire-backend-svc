package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private Instant createdAfter;
    private Instant createdBefore;
    private String createdBy;
}
