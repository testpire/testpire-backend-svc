package com.testpire.testpire.dto.response;

public record LogoutResponseDto(
    String message,
    boolean success
) {
    public static LogoutResponseDto successResponse() {
        return new LogoutResponseDto("Logout successful", true);
    }
    
    public static LogoutResponseDto errorResponse(String message) {
        return new LogoutResponseDto(message, false);
    }
}
