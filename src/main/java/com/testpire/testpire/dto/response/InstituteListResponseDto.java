package com.testpire.testpire.dto.response;

import java.util.List;

public record InstituteListResponseDto(
    String message,
    boolean success,
    List<InstituteResponseDto> institutes,
    int totalCount,
    int page,
    int size
) {
    public static InstituteListResponseDto success(List<InstituteResponseDto> institutes, int totalCount, int page, int size) {
        return new InstituteListResponseDto(
            "Institutes retrieved successfully",
            true,
            institutes,
            totalCount,
            page,
            size
        );
    }
    
    public static InstituteListResponseDto error(String message) {
        return new InstituteListResponseDto(message, false, List.of(), 0, 0, 0);
    }
}
