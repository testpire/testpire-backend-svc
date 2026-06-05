package com.testpire.testpire.dto.response;

import java.util.List;

public record LeadListResponseDto(
    String message,
    boolean success,
    List<LeadResponseDto> leads,
    int totalCount,
    int page,
    int size
) {
    public static LeadListResponseDto success(List<LeadResponseDto> leads, int totalCount, int page, int size) {
        return new LeadListResponseDto(
            "Leads retrieved successfully",
            true,
            leads,
            totalCount,
            page,
            size
        );
    }

    public static LeadListResponseDto error(String message) {
        return new LeadListResponseDto(message, false, List.of(), 0, 0, 0);
    }
}
