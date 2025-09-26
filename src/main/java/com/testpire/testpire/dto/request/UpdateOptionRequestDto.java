package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record UpdateOptionRequestDto(
    @NotNull(message = "Option ID is required")
    Long id,
    
    @NotBlank(message = "Option text is required")
    @Size(max = 1000, message = "Option text cannot exceed 1000 characters")
    String text,
    
    String optionImagePath,
    
    @NotNull(message = "Option order is required")
    @Min(value = 1, message = "Option order must be at least 1")
    @Max(value = 6, message = "Option order cannot exceed 6")
    Integer optionOrder,
    
    @NotNull(message = "Is correct flag is required")
    Boolean isCorrect
) {}
