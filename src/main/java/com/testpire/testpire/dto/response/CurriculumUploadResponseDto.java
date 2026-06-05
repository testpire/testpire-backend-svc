package com.testpire.testpire.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CurriculumUploadResponseDto(
        int totalRows,
        int subjectsCreated,
        int chaptersCreated,
        int topicsCreated,
        int subjectsReused,
        int chaptersReused,
        int topicsReused,
        List<String> errors
) {}
