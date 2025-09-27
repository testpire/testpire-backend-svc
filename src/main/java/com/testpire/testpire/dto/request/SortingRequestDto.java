package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SortingRequestDto {
    
    @Pattern(regexp = "^(createdAt|updatedAt|name|code|id)$", message = "Sort field must be one of: createdAt, updatedAt, name, code, id")
    @Builder.Default
    private String field = "createdAt";
    
    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    @Builder.Default
    private String direction = "desc";
}
