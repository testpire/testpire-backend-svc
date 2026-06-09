package com.testpire.testpire.dto.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponseDto(
    String message,
    String error,
    int status,
    Instant timestamp,
    String path,
    List<String> details
) {
    public static ErrorResponseDto of(String message, String error, int status, String path) {
        return new ErrorResponseDto(
            message,
            error,
            status,
            Instant.now(),
            path,
            List.of()
        );
    }
    
    public static ErrorResponseDto of(String message, String error, int status, String path, List<String> details) {
        return new ErrorResponseDto(
            message,
            error,
            status,
            Instant.now(),
            path,
            details
        );
    }
}
