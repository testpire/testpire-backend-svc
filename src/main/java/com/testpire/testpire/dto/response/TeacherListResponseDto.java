package com.testpire.testpire.dto.response;

import java.util.List;

public record TeacherListResponseDto(
    String message,
    boolean success,
    List<TeacherResponseDto> teachers,
    int totalCount,
    int page,
    int size
) {
    public static TeacherListResponseDto success(List<TeacherResponseDto> teachers, int totalCount, int page, int size) {
        return new TeacherListResponseDto(
            "Teachers retrieved successfully",
            true,
            teachers,
            totalCount,
            page,
            size
        );
    }
    
    public static TeacherListResponseDto error(String message) {
        return new TeacherListResponseDto(message, false, List.of(), 0, 0, 0);
    }
}
