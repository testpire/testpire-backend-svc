package com.testpire.testpire.dto.response;

import java.util.List;

public record UserListResponseDto(
    String message,
    boolean success,
    List<UserResponseDto> users,
    int totalCount,
    int page,
    int size
) {
    public static UserListResponseDto success(List<UserResponseDto> users, int totalCount, int page, int size) {
        return new UserListResponseDto(
            "Users retrieved successfully",
            true,
            users,
            totalCount,
            page,
            size
        );
    }
    
    public static UserListResponseDto error(String message) {
        return new UserListResponseDto(message, false, List.of(), 0, 0, 0);
    }
}
