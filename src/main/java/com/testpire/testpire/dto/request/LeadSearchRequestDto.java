package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadSearchRequestDto {

    @Valid
    private LeadCriteriaDto criteria;

    @Valid
    private PaginationRequestDto pagination;

    @Valid
    private SortingRequestDto sorting;

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

    public String getEmail() {
        return criteria != null ? criteria.getEmail() : null;
    }

    public String getPhone() {
        return criteria != null ? criteria.getPhone() : null;
    }

    public LeadStatus getStatus() {
        return criteria != null ? criteria.getStatus() : null;
    }

    public LeadSource getSource() {
        return criteria != null ? criteria.getSource() : null;
    }

    public Long getInterestedCourseId() {
        return criteria != null ? criteria.getInterestedCourseId() : null;
    }

    public String getAssignedTo() {
        return criteria != null ? criteria.getAssignedTo() : null;
    }

    public Boolean getConverted() {
        return criteria != null ? criteria.getConverted() : null;
    }

    public LocalDate getFollowUpFrom() {
        return criteria != null ? criteria.getFollowUpFrom() : null;
    }

    public LocalDate getFollowUpTo() {
        return criteria != null ? criteria.getFollowUpTo() : null;
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
