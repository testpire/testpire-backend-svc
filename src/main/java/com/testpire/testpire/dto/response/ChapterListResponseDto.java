package com.testpire.testpire.dto.response;

import java.util.List;

public record ChapterListResponseDto(
        List<ChapterResponseDto> chapters,
        int totalCount
) {
    public static ChapterListResponseDto of(List<ChapterResponseDto> chapters) {
        return new ChapterListResponseDto(chapters, chapters.size());
    }

    public static ChapterListResponseDto of(List<ChapterResponseDto> chapters, long totalCount) {
        return new ChapterListResponseDto(chapters, (int) totalCount);
    }
}


