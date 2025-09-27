package com.testpire.testpire.dto.response;

import java.util.List;

public record TopicListResponseDto(
        List<TopicResponseDto> topics,
        int totalCount
) {
    public static TopicListResponseDto of(List<TopicResponseDto> topics) {
        return new TopicListResponseDto(topics, topics.size());
    }

    public static TopicListResponseDto of(List<TopicResponseDto> topics, long totalCount) {
        return new TopicListResponseDto(topics, (int) totalCount);
    }
}


