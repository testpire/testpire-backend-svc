package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateChapterRequestDto;
import com.testpire.testpire.dto.request.UpdateChapterRequestDto;
import com.testpire.testpire.dto.response.ChapterListResponseDto;
import com.testpire.testpire.dto.response.ChapterResponseDto;
import com.testpire.testpire.entity.Chapter;
import com.testpire.testpire.entity.Subject;
import com.testpire.testpire.repository.ChapterRepository;
import com.testpire.testpire.repository.SubjectRepository;
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
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public ChapterResponseDto createChapter(CreateChapterRequestDto request) {
        log.info("Creating chapter: {} for subject: {} in institute: {}", 
                request.name(), request.subjectId(), request.instituteId());

        // Verify subject exists and belongs to the same institute
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + request.subjectId()));

        if (!subject.getInstituteId().equals(request.instituteId())) {
            throw new IllegalArgumentException("Subject does not belong to the specified institute");
        }

        // Check if chapter code already exists in the institute
        if (chapterRepository.existsByCodeAndInstituteId(request.code(), request.instituteId())) {
            throw new IllegalArgumentException("Chapter with code " + request.code() + " already exists in this institute");
        }

        Chapter chapter = Chapter.builder()
                .name(request.name())
                .description(request.description())
                .code(request.code())
                .subject(subject)
                .instituteId(request.instituteId())
                .orderIndex(request.orderIndex())
                .duration(request.duration())
                .objectives(request.objectives())
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Chapter created successfully with ID: {}", savedChapter.getId());
        return ChapterResponseDto.fromEntity(savedChapter);
    }

    @Transactional
    public ChapterResponseDto updateChapter(Long id, UpdateChapterRequestDto request) {
        log.info("Updating chapter with ID: {}", id);

        Chapter existingChapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with ID: " + id));

        // Check if new code conflicts with existing chapters in the same institute
        if (request.code() != null && !request.code().equals(existingChapter.getCode()) &&
            chapterRepository.existsByCodeAndInstituteId(request.code(), existingChapter.getInstituteId())) {
            throw new IllegalArgumentException("Chapter with code " + request.code() + " already exists in this institute");
        }

        Optional.ofNullable(request.name()).ifPresent(existingChapter::setName);
        Optional.ofNullable(request.description()).ifPresent(existingChapter::setDescription);
        Optional.ofNullable(request.code()).ifPresent(existingChapter::setCode);
        Optional.ofNullable(request.orderIndex()).ifPresent(existingChapter::setOrderIndex);
        Optional.ofNullable(request.duration()).ifPresent(existingChapter::setDuration);
        Optional.ofNullable(request.objectives()).ifPresent(existingChapter::setObjectives);
        Optional.ofNullable(request.active()).ifPresent(existingChapter::setActive);
        existingChapter.setUpdatedBy(RequestUtils.getCurrentUsername());

        Chapter updatedChapter = chapterRepository.save(existingChapter);
        log.info("Chapter updated successfully with ID: {}", updatedChapter.getId());
        return ChapterResponseDto.fromEntity(updatedChapter);
    }

    @Transactional
    public void deleteChapter(Long id) {
        log.info("Deleting chapter with ID: {}", id);

        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with ID: " + id));

        chapter.setActive(false);
        chapter.setUpdatedBy(RequestUtils.getCurrentUsername());
        chapterRepository.save(chapter);
        log.info("Chapter deactivated successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public ChapterResponseDto getChapterById(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with ID: " + id));
        return ChapterResponseDto.fromEntity(chapter);
    }

    @Transactional(readOnly = true)
    public ChapterResponseDto getChapterByCode(String code, Long instituteId) {
        Chapter chapter = chapterRepository.findByCodeAndInstituteId(code, instituteId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found with code: " + code));
        return ChapterResponseDto.fromEntity(chapter);
    }

    @Transactional(readOnly = true)
    public ChapterListResponseDto getChaptersByInstitute(Long instituteId) {
        List<Chapter> chapters = chapterRepository.findByInstituteIdAndActiveTrue(instituteId);
        List<ChapterResponseDto> chapterDtos = chapters.stream()
                .map(ChapterResponseDto::fromEntity)
                .toList();
        return ChapterListResponseDto.of(chapterDtos);
    }

    @Transactional(readOnly = true)
    public ChapterListResponseDto getChaptersBySubject(Long subjectId, Long instituteId) {
        List<Chapter> chapters = chapterRepository.findBySubjectIdAndInstituteIdAndActiveTrue(subjectId, instituteId);
        List<ChapterResponseDto> chapterDtos = chapters.stream()
                .map(ChapterResponseDto::fromEntity)
                .toList();
        return ChapterListResponseDto.of(chapterDtos);
    }

    @Transactional(readOnly = true)
    public ChapterListResponseDto searchChapters(Long instituteId, String query) {
        List<Chapter> chapters = chapterRepository.findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(
                instituteId, query, instituteId, query);
        List<ChapterResponseDto> chapterDtos = chapters.stream()
                .map(ChapterResponseDto::fromEntity)
                .toList();
        return ChapterListResponseDto.of(chapterDtos);
    }

    @Transactional(readOnly = true)
    public ChapterListResponseDto getAllChapters() {
        List<Chapter> chapters = chapterRepository.findAll();
        List<ChapterResponseDto> chapterDtos = chapters.stream()
                .map(ChapterResponseDto::fromEntity)
                .toList();
        return ChapterListResponseDto.of(chapterDtos);
    }
}

