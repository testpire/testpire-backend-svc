package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateTopicRequestDto;
import com.testpire.testpire.dto.request.UpdateTopicRequestDto;
import com.testpire.testpire.dto.response.TopicListResponseDto;
import com.testpire.testpire.dto.response.TopicResponseDto;
import com.testpire.testpire.entity.Chapter;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.repository.ChapterRepository;
import com.testpire.testpire.repository.TopicRepository;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final TopicRepository topicRepository;
    private final ChapterRepository chapterRepository;

    @Transactional
    public TopicResponseDto createTopic(CreateTopicRequestDto request) {
        log.info("Creating topic: {} for chapter: {} in institute: {}", 
                request.name(), request.chapterId(), request.instituteId());

        // Verify chapter exists and belongs to the same institute
        Chapter chapter = chapterRepository.findById(request.chapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with ID: " + request.chapterId()));

        if (!chapter.getInstituteId().equals(request.instituteId())) {
            throw new IllegalArgumentException("Chapter does not belong to the specified institute");
        }

        // Check if topic code already exists in the institute
        if (topicRepository.existsByCodeAndInstituteId(request.code(), request.instituteId())) {
            throw new IllegalArgumentException("Topic with code " + request.code() + " already exists in this institute");
        }

        Topic topic = Topic.builder()
                .name(request.name())
                .description(request.description())
                .code(request.code())
                .chapter(chapter)
                .instituteId(request.instituteId())
                .orderIndex(request.orderIndex())
                .duration(request.duration())
                .content(request.content())
                .learningOutcomes(request.learningOutcomes())
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        Topic savedTopic = topicRepository.save(topic);
        log.info("Topic created successfully with ID: {}", savedTopic.getId());
        return TopicResponseDto.fromEntity(savedTopic);
    }

    @Transactional
    public TopicResponseDto updateTopic(Long id, UpdateTopicRequestDto request) {
        log.info("Updating topic with ID: {}", id);

        Topic existingTopic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + id));

        // Check if new code conflicts with existing topics in the same institute
        if (request.code() != null && !request.code().equals(existingTopic.getCode()) &&
            topicRepository.existsByCodeAndInstituteId(request.code(), existingTopic.getInstituteId())) {
            throw new IllegalArgumentException("Topic with code " + request.code() + " already exists in this institute");
        }

        Optional.ofNullable(request.name()).ifPresent(existingTopic::setName);
        Optional.ofNullable(request.description()).ifPresent(existingTopic::setDescription);
        Optional.ofNullable(request.code()).ifPresent(existingTopic::setCode);
        Optional.ofNullable(request.orderIndex()).ifPresent(existingTopic::setOrderIndex);
        Optional.ofNullable(request.duration()).ifPresent(existingTopic::setDuration);
        Optional.ofNullable(request.content()).ifPresent(existingTopic::setContent);
        Optional.ofNullable(request.learningOutcomes()).ifPresent(existingTopic::setLearningOutcomes);
        Optional.ofNullable(request.active()).ifPresent(existingTopic::setActive);
        existingTopic.setUpdatedBy(RequestUtils.getCurrentUsername());

        Topic updatedTopic = topicRepository.save(existingTopic);
        log.info("Topic updated successfully with ID: {}", updatedTopic.getId());
        return TopicResponseDto.fromEntity(updatedTopic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        log.info("Deleting topic with ID: {}", id);

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + id));

        topic.setActive(false);
        topic.setUpdatedBy(RequestUtils.getCurrentUsername());
        topicRepository.save(topic);
        log.info("Topic deactivated successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public TopicResponseDto getTopicById(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + id));
        return TopicResponseDto.fromEntity(topic);
    }

    @Transactional(readOnly = true)
    public TopicResponseDto getTopicByCode(String code, Long instituteId) {
        Topic topic = topicRepository.findByCodeAndInstituteId(code, instituteId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with code: " + code));
        return TopicResponseDto.fromEntity(topic);
    }

    @Transactional(readOnly = true)
    public TopicListResponseDto getTopicsByInstitute(Long instituteId) {
        List<Topic> topics = topicRepository.findByInstituteIdAndActiveTrue(instituteId);
        List<TopicResponseDto> topicDtos = topics.stream()
                .map(TopicResponseDto::fromEntity)
                .toList();
        return TopicListResponseDto.of(topicDtos);
    }

    @Transactional(readOnly = true)
    public TopicListResponseDto getTopicsByChapter(Long chapterId, Long instituteId) {
        List<Topic> topics = topicRepository.findByChapterIdAndInstituteIdAndActiveTrue(chapterId, instituteId);
        List<TopicResponseDto> topicDtos = topics.stream()
                .map(TopicResponseDto::fromEntity)
                .toList();
        return TopicListResponseDto.of(topicDtos);
    }

    @Transactional(readOnly = true)
    public TopicListResponseDto searchTopics(Long instituteId, String query) {
        List<Topic> topics = topicRepository.findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(
                instituteId, query, instituteId, query);
        List<TopicResponseDto> topicDtos = topics.stream()
                .map(TopicResponseDto::fromEntity)
                .toList();
        return TopicListResponseDto.of(topicDtos);
    }

    @Transactional(readOnly = true)
    public TopicListResponseDto getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicResponseDto> topicDtos = topics.stream()
                .map(TopicResponseDto::fromEntity)
                .toList();
        return TopicListResponseDto.of(topicDtos);
    }
}
