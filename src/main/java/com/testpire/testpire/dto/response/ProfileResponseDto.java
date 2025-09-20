package com.testpire.testpire.dto.response;

import com.testpire.testpire.dto.UserDto;

public record ProfileResponseDto(
    String message,
    boolean success,
    UserDto user
) {
    public static ProfileResponseDto success(UserDto user) {
        return new ProfileResponseDto("Profile retrieved successfully", true, user);
    }
    
    public static ProfileResponseDto error(String message) {
        return new ProfileResponseDto(message, false, null);
    }
}
