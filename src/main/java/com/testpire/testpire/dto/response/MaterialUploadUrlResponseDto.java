package com.testpire.testpire.dto.response;

/**
 * Response to an upload-URL request: a short-lived presigned S3 PUT URL plus the {@code s3Key} the
 * client must echo back when registering the material. The client uploads with
 * {@code Content-Type: contentType} (it is part of the signature) and within {@code expiresInSeconds}.
 */
public record MaterialUploadUrlResponseDto(
        String uploadUrl,
        String s3Key,
        String contentType,
        long expiresInSeconds
) {}
