package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TopicSearchRequestDto {
    
    @Valid
    private TopicCriteriaDto criteria;
    
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
    
    public String getSearchText() {
        return criteria != null ? criteria.getSearchText() : null;
    }
    
    public String getName() {
        return criteria != null ? criteria.getName() : null;
    }
    
    public String getCode() {
        return criteria != null ? criteria.getCode() : null;
    }
    
    public String getDescription() {
        return criteria != null ? criteria.getDescription() : null;
    }
    
    public String getContent() {
        return criteria != null ? criteria.getContent() : null;
    }
    
    public String getLearningOutcomes() {
        return criteria != null ? criteria.getLearningOutcomes() : null;
    }
    
    public Integer getMinOrderIndex() {
        return criteria != null ? criteria.getMinOrderIndex() : null;
    }
    
    public Integer getMaxOrderIndex() {
        return criteria != null ? criteria.getMaxOrderIndex() : null;
    }
    
    public Integer getMinDuration() {
        return criteria != null ? criteria.getMinDuration() : null;
    }
    
    public Integer getMaxDuration() {
        return criteria != null ? criteria.getMaxDuration() : null;
    }
    
    public Boolean getActive() {
        return criteria != null ? criteria.getActive() : null;
    }
    
    public Boolean getHasQuestions() {
        return criteria != null ? criteria.getHasQuestions() : null;
    }
    
    public Integer getMinQuestions() {
        return criteria != null ? criteria.getMinQuestions() : null;
    }
    
    public Integer getMaxQuestions() {
        return criteria != null ? criteria.getMaxQuestions() : null;
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