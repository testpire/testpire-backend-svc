package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.MaterialType;
import com.testpire.testpire.enums.TextFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Register a material against a topic. The required payload fields depend on {@code type}:
 * <ul>
 *   <li>file-backed (PDF/PPT/VIDEO): {@code s3Key} (from a prior upload-URL request) is required;
 *       {@code fileName}/{@code contentType} are stored as metadata. The service verifies the S3
 *       object actually exists before persisting.</li>
 *   <li>NOTE: {@code content} is required; {@code contentFormat} defaults to PLAIN.</li>
 *   <li>LINK: {@code externalUrl} is required.</li>
 * </ul>
 * The owning topic comes from the path; {@code instituteId} is resolved from the caller's JWT.
 */
public record CreateTopicMaterialRequestDto(
        @NotNull(message = "type is required")
        MaterialType type,

        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must not exceed 255 characters")
        String title,

        @Size(max = 2000, message = "description must not exceed 2000 characters")
        String description,

        // file-backed
        String s3Key,
        String fileName,
        String contentType,

        // NOTE
        String content,
        TextFormat contentFormat,

        // LINK
        String externalUrl,

        Integer sortOrder
) {}
