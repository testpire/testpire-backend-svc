package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSearchRequestDto {
    
    @Valid
    private StudentCriteriaDto criteria;
    
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
    
    public String getSearchText() {
        return criteria != null ? criteria.getSearchText() : null;
    }
    
    public String getFirstName() {
        return criteria != null ? criteria.getFirstName() : null;
    }
    
    public String getLastName() {
        return criteria != null ? criteria.getLastName() : null;
    }
    
    public String getUsername() {
        return criteria != null ? criteria.getUsername() : null;
    }
    
    public String getEmail() {
        return criteria != null ? criteria.getEmail() : null;
    }
    
    public String getPhone() {
        return criteria != null ? criteria.getPhone() : null;
    }
    
    public String getCourse() {
        return criteria != null ? criteria.getCourse() : null;
    }

    public Long getCourseId() {
        return criteria != null ? criteria.getCourseId() : null;
    }

    public Long getBatchId() {
        return criteria != null ? criteria.getBatchId() : null;
    }

    public Integer getMinCurrentClass() {
        return criteria != null ? criteria.getMinCurrentClass() : null;
    }

    public Integer getMaxCurrentClass() {
        return criteria != null ? criteria.getMaxCurrentClass() : null;
    }

    public String getRollNumber() {
        return criteria != null ? criteria.getRollNumber() : null;
    }
    
    public String getParentName() {
        return criteria != null ? criteria.getParentName() : null;
    }
    
    public String getParentPhone() {
        return criteria != null ? criteria.getParentPhone() : null;
    }
    
    public String getParentEmail() {
        return criteria != null ? criteria.getParentEmail() : null;
    }
    
    public String getAddress() {
        return criteria != null ? criteria.getAddress() : null;
    }
    
    public String getBloodGroup() {
        return criteria != null ? criteria.getBloodGroup() : null;
    }
    
    public String getEmergencyContact() {
        return criteria != null ? criteria.getEmergencyContact() : null;
    }
    
    public Boolean getEnabled() {
        return criteria != null ? criteria.getEnabled() : null;
    }
    
    public Instant getCreatedAfter() {
        return criteria != null ? criteria.getCreatedAfter() : null;
    }
    
    public Instant getCreatedBefore() {
        return criteria != null ? criteria.getCreatedBefore() : null;
    }
    
    public String getCreatedBy() {
        return criteria != null ? criteria.getCreatedBy() : null;
    }
}
