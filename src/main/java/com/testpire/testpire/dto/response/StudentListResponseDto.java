package com.testpire.testpire.dto.response;

import java.util.List;

public record StudentListResponseDto(
    String message,
    boolean success,
    List<StudentResponseDto> students,
    int totalCount,
    int page,
    int size
) {
    public static StudentListResponseDto success(List<StudentResponseDto> students, int totalCount, int page, int size) {
        return new StudentListResponseDto(
            "Students retrieved successfully",
            true,
            students,
            totalCount,
            page,
            size
        );
    }
    
    public static StudentListResponseDto error(String message) {
        return new StudentListResponseDto(message, false, List.of(), 0, 0, 0);
    }
}
