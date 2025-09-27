package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.DifficultyLevel;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuestionSearchRequestDto {
    
    @Valid
    private QuestionCriteriaDto criteria;
    
    @Valid
    private PaginationRequestDto pagination;
    
    @Valid
    private SortingRequestDto sorting;
    
    // Helper methods for backward compatibility
    public Integer getPage() {
        return pagination != null ? pagination.getPage() : 0;
    }
    
    public Integer getSize() {
        return pagination != null ? pagination.getSize() : 20;
    }
    
    public String getSortBy() {
        return sorting != null ? sorting.getField() : "createdAt";
    }
    
    public String getSortDirection() {
        return sorting != null ? sorting.getDirection() : "desc";
    }
    
    // Delegate methods to criteria
    public Long getInstituteId() {
        return criteria != null ? criteria.getInstituteId() : null;
    }
    
    public Long getCourseId() {
        return criteria != null ? criteria.getCourseId() : null;
    }
    
    public Long getSubjectId() {
        return criteria != null ? criteria.getSubjectId() : null;
    }
    
    public Long getChapterId() {
        return criteria != null ? criteria.getChapterId() : null;
    }
    
    public Long getTopicId() {
        return criteria != null ? criteria.getTopicId() : null;
    }
    
    public String getSearchText() {
        return criteria != null ? criteria.getSearchText() : null;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return criteria != null ? criteria.getDifficultyLevel() : null;
    }
    
    public String getQuestionType() {
        return criteria != null ? criteria.getQuestionType() : null;
    }
    
    public Integer getMinMarks() {
        return criteria != null ? criteria.getMinMarks() : null;
    }
    
    public Integer getMaxMarks() {
        return criteria != null ? criteria.getMaxMarks() : null;
    }
    
    public Integer getMinNegativeMarks() {
        return criteria != null ? criteria.getMinNegativeMarks() : null;
    }
    
    public Integer getMaxNegativeMarks() {
        return criteria != null ? criteria.getMaxNegativeMarks() : null;
    }
    
    public Boolean getHasQuestionImage() {
        return criteria != null ? criteria.getHasQuestionImage() : null;
    }
    
    public Boolean getHasExplanation() {
        return criteria != null ? criteria.getHasExplanation() : null;
    }
    
    public Boolean getHasCorrectOption() {
        return criteria != null ? criteria.getHasCorrectOption() : null;
    }
    
    public Boolean getHasOptions() {
        return criteria != null ? criteria.getHasOptions() : null;
    }
    
    public Integer getMinOptions() {
        return criteria != null ? criteria.getMinOptions() : null;
    }
    
    public Integer getMaxOptions() {
        return criteria != null ? criteria.getMaxOptions() : null;
    }
    
    public Boolean getActive() {
        return criteria != null ? criteria.getActive() : null;
    }
    
    public LocalDateTime getCreatedAfter() {
        return criteria != null ? criteria.getCreatedAfter() : null;
    }
    
    public LocalDateTime getCreatedBefore() {
        return criteria != null ? criteria.getCreatedBefore() : null;
    }
    
    public String getCreatedBy() {
        return criteria != null ? criteria.getCreatedBy() : null;
    }
}