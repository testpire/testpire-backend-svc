package com.testpire.testpire.dto.response;

public record ApiResponseDto(
    String message,
    boolean success,
    Object data
) {
    public static ApiResponseDto success(String message, Object data) {
        return new ApiResponseDto(message, true, data);
    }
    
    public static ApiResponseDto success(String message) {
        return new ApiResponseDto(message, true, null);
    }
    
    public static ApiResponseDto error(String message) {
        return new ApiResponseDto(message, false, null);
    }
    
    public static ApiResponseDto error(String message, Object data) {
        return new ApiResponseDto(message, false, data);
    }
}
