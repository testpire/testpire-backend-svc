package com.testpire.testpire.dto.response;

import java.util.List;

public record SubjectListResponseDto(
        List<SubjectResponseDto> subjects,
        int totalCount
) {
    public static SubjectListResponseDto of(List<SubjectResponseDto> subjects) {
        return new SubjectListResponseDto(subjects, subjects.size());
    }

    public static SubjectListResponseDto of(List<SubjectResponseDto> subjects, long totalCount) {
        return new SubjectListResponseDto(subjects, (int) totalCount);
    }
}


