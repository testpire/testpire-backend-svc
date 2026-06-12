package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.TextFormat;
import jakarta.validation.constraints.Size;

/**
 * Update a material's metadata (and, for NOTE/LINK, its payload). All fields are optional — only
 * non-null fields are applied. The underlying file of a file-backed material is not replaced here;
 * to swap the file, delete the material and create a new one.
 */
public record UpdateTopicMaterialRequestDto(
        @Size(max = 255, message = "title must not exceed 255 characters")
        String title,

        @Size(max = 2000, message = "description must not exceed 2000 characters")
        String description,

        String content,
        TextFormat contentFormat,
        String externalUrl,
        Integer sortOrder
) {}
