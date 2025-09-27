package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TeacherSearchRequestDto {
    
    @Valid
    private TeacherCriteriaDto criteria;
    
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
    
    public String getDepartment() {
        return criteria != null ? criteria.getDepartment() : null;
    }
    
    public String getSubject() {
        return criteria != null ? criteria.getSubject() : null;
    }
    
    public String getQualification() {
        return criteria != null ? criteria.getQualification() : null;
    }
    
    public Integer getMinExperienceYears() {
        return criteria != null ? criteria.getMinExperienceYears() : null;
    }
    
    public Integer getMaxExperienceYears() {
        return criteria != null ? criteria.getMaxExperienceYears() : null;
    }
    
    public String getSpecialization() {
        return criteria != null ? criteria.getSpecialization() : null;
    }
    
    public String getBio() {
        return criteria != null ? criteria.getBio() : null;
    }
    
    public Boolean getEnabled() {
        return criteria != null ? criteria.getEnabled() : null;
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
