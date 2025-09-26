package com.testpire.testpire.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record QuestionListResponseDto(
    List<QuestionResponseDto> questions,
    Long totalCount
) {}

