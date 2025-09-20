package com.testpire.testpire.dto.response;

import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.enums.UserRole;

public record LoginResponseDto(
    String message,
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UserDto user
) {
    public static LoginResponseDto success(String accessToken, String refreshToken, Long expiresIn, UserDto user) {
        return new LoginResponseDto(
            "Login successful",
            accessToken,
            refreshToken,
            "Bearer",
            expiresIn,
            user
        );
    }
}
