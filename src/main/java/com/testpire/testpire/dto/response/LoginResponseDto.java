package com.testpire.testpire.dto.response;

import com.testpire.testpire.dto.UserDto;

public record LoginResponseDto(
    String message,
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UserDto user,
    String challengeName,
    String session
) {
    public static LoginResponseDto success(String accessToken, String refreshToken, Long expiresIn, UserDto user) {
        return new LoginResponseDto(
            "Login successful", accessToken, refreshToken, "Bearer", expiresIn, user, null, null
        );
    }

    public static LoginResponseDto challenge(String challengeName, String session) {
        return new LoginResponseDto(
            "Password change required", null, null, null, null, null, challengeName, session
        );
    }
}
