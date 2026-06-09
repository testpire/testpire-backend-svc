package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record CreateOptionRequestDto(
    @NotBlank(message = "Option text is required")
    @Size(max = 2000, message = "Option text cannot exceed 2000 characters")
    String text,
    
    String optionImagePath,
    
    @NotNull(message = "Is correct flag is required")
    Boolean isCorrect
) {}
