package com.testpire.testpire.dto.response;

import java.util.List;

public record ChapterListResponseDto(
        List<ChapterResponseDto> chapters,
        int totalCount
) {
    public static ChapterListResponseDto of(List<ChapterResponseDto> chapters) {
        return new ChapterListResponseDto(chapters, chapters.size());
    }
}
