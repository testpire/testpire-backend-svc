package com.testpire.testpire.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Standard response envelope for every endpoint, success or failure.
 *
 * <p>{@code data} carries the payload on success. {@code errors} is a field -> message map used
 * only for validation failures (so the frontend can highlight the offending field); it is omitted
 * from the JSON when null so non-validation responses stay clean.
 */
public record ApiResponseDto(
    String message,
    boolean success,
    Object data,
    @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors
) {
    public static ApiResponseDto success(String message, Object data) {
        return new ApiResponseDto(message, true, data, null);
    }

    public static ApiResponseDto success(String message) {
        return new ApiResponseDto(message, true, null, null);
    }

    public static ApiResponseDto error(String message) {
        return new ApiResponseDto(message, false, null, null);
    }

    public static ApiResponseDto error(String message, Object data) {
        return new ApiResponseDto(message, false, data, null);
    }

    /** Field-level validation failure: {@code errors} maps each rejected field to its message. */
    public static ApiResponseDto validationError(String message, Map<String, String> errors) {
        return new ApiResponseDto(message, false, null, errors);
    }
}
