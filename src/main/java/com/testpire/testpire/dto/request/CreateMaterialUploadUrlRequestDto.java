package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Step 1 of a file-backed material upload: ask for a presigned S3 PUT URL.
 *
 * <p>The client declares the file it intends to upload; the service validates the content type and
 * declared size against the allowlist/limit before minting the URL. The actual bytes never pass
 * through the service — the client PUTs them straight to S3, then registers the row via
 * {@link CreateTopicMaterialRequestDto} (which re-verifies the object server-side).</p>
 */
public record CreateMaterialUploadUrlRequestDto(
        @NotBlank(message = "fileName is required")
        String fileName,

        @NotBlank(message = "contentType is required")
        String contentType,

        @NotNull(message = "sizeBytes is required")
        @Positive(message = "sizeBytes must be positive")
        Long sizeBytes
) {}
