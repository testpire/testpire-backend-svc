package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public abstract class BaseSearchRequestDto {
    
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
}
