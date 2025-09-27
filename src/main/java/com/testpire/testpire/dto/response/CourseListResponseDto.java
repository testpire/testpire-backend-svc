package com.testpire.testpire.dto.response;

import java.util.List;

public record CourseListResponseDto(
        List<CourseResponseDto> courses,
        int totalCount
) {
    public static CourseListResponseDto of(List<CourseResponseDto> courses) {
        return new CourseListResponseDto(courses, courses.size());
    }
    
    public static CourseListResponseDto of(List<CourseResponseDto> courses, long totalCount) {
        return new CourseListResponseDto(courses, (int) totalCount);
    }
}


