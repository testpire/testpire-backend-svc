package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubjectSearchRequestDto {
    
    @Valid
    private SubjectCriteriaDto criteria;
    
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
    
    public Integer getMinDuration() {
        return criteria != null ? criteria.getMinDuration() : null;
    }
    
    public Integer getMaxDuration() {
        return criteria != null ? criteria.getMaxDuration() : null;
    }
    
    public Integer getMinCredits() {
        return criteria != null ? criteria.getMinCredits() : null;
    }
    
    public Integer getMaxCredits() {
        return criteria != null ? criteria.getMaxCredits() : null;
    }
    
    public String getPrerequisites() {
        return criteria != null ? criteria.getPrerequisites() : null;
    }
    
    public Boolean getActive() {
        return criteria != null ? criteria.getActive() : null;
    }
    
    public Boolean getHasChapters() {
        return criteria != null ? criteria.getHasChapters() : null;
    }
    
    public Integer getMinChapters() {
        return criteria != null ? criteria.getMinChapters() : null;
    }
    
    public Integer getMaxChapters() {
        return criteria != null ? criteria.getMaxChapters() : null;
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