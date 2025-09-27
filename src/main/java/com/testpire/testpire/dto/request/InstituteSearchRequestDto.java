package com.testpire.testpire.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstituteSearchRequestDto {
    
    private InstituteCriteriaDto criteria;
    private PaginationRequestDto pagination;
    private SortingRequestDto sorting;
}
