package com.testpire.testpire.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OptionSearchRequestDto {
    
    private Long instituteId;
    private Long courseId;
    private Long subjectId;
    private Long chapterId;
    private Long topicId;
    private Long questionId;
    private String searchText;
    private String text;
    private Boolean hasOptionImage;
    private Integer minOrder;
    private Integer maxOrder;
    private Boolean isCorrect;
    private Boolean active;
    private String questionType;
    private String difficultyLevel;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String createdBy;
    
    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
    
    // Default values
    public static OptionSearchRequestDto getDefault() {
        return OptionSearchRequestDto.builder()
                .page(0)
                .size(20)
                .sortBy("optionOrder")
                .sortDirection("asc")
                .active(true)
                .build();
    }
}
