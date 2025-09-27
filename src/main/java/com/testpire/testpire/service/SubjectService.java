package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateSubjectRequestDto;
import com.testpire.testpire.dto.request.SubjectSearchRequestDto;
import com.testpire.testpire.dto.request.UpdateSubjectRequestDto;
import com.testpire.testpire.repository.specification.SubjectSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.testpire.testpire.dto.response.SubjectListResponseDto;
import com.testpire.testpire.dto.response.SubjectResponseDto;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.entity.Subject;
import com.testpire.testpire.repository.CourseRepository;
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
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public SubjectResponseDto createSubject(CreateSubjectRequestDto request) {
        log.info("Creating subject: {} for course: {} in institute: {}", 
                request.name(), request.courseId(), request.instituteId());

        // Verify course exists and belongs to the same institute
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + request.courseId()));

        if (!course.getInstituteId().equals(request.instituteId())) {
            throw new IllegalArgumentException("Course does not belong to the specified institute");
        }

        // Check if subject code already exists in the institute
        if (subjectRepository.existsByCodeAndInstituteId(request.code(), request.instituteId())) {
            throw new IllegalArgumentException("Subject with code " + request.code() + " already exists in this institute");
        }

        Subject subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .code(request.code())
                .course(course)
                .instituteId(request.instituteId())
                .duration(request.duration())
                .credits(request.credits())
                .prerequisites(request.prerequisites())
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Subject created successfully with ID: {}", savedSubject.getId());
        return SubjectResponseDto.fromEntity(savedSubject);
    }

    @Transactional
    public SubjectResponseDto updateSubject(Long id, UpdateSubjectRequestDto request) {
        log.info("Updating subject with ID: {}", id);

        Subject existingSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + id));

        // Check if new code conflicts with existing subjects in the same institute
        if (request.code() != null && !request.code().equals(existingSubject.getCode()) &&
            subjectRepository.existsByCodeAndInstituteId(request.code(), existingSubject.getInstituteId())) {
            throw new IllegalArgumentException("Subject with code " + request.code() + " already exists in this institute");
        }

        Optional.ofNullable(request.name()).ifPresent(existingSubject::setName);
        Optional.ofNullable(request.description()).ifPresent(existingSubject::setDescription);
        Optional.ofNullable(request.code()).ifPresent(existingSubject::setCode);
        Optional.ofNullable(request.duration()).ifPresent(existingSubject::setDuration);
        Optional.ofNullable(request.credits()).ifPresent(existingSubject::setCredits);
        Optional.ofNullable(request.prerequisites()).ifPresent(existingSubject::setPrerequisites);
        Optional.ofNullable(request.active()).ifPresent(existingSubject::setActive);
        existingSubject.setUpdatedBy(RequestUtils.getCurrentUsername());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        log.info("Subject updated successfully with ID: {}", updatedSubject.getId());
        return SubjectResponseDto.fromEntity(updatedSubject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        log.info("Deleting subject with ID: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + id));

        subject.setActive(false);
        subject.setUpdatedBy(RequestUtils.getCurrentUsername());
        subjectRepository.save(subject);
        log.info("Subject deactivated successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public SubjectResponseDto getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with ID: " + id));
        return SubjectResponseDto.fromEntity(subject);
    }

    @Transactional(readOnly = true)
    public SubjectResponseDto getSubjectByCode(String code, Long instituteId) {
        Subject subject = subjectRepository.findByCodeAndInstituteId(code, instituteId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with code: " + code));
        return SubjectResponseDto.fromEntity(subject);
    }

    @Transactional(readOnly = true)
    public SubjectListResponseDto searchSubjectsWithSpecification(SubjectSearchRequestDto request) {
        log.info("Searching subjects with specification: {}", request);

        // Build specification
        Specification<Subject> spec = buildSpecification(request);

        // Create pageable
        Pageable pageable = createPageable(request);

        // Execute search
        Page<Subject> subjectPage = subjectRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<SubjectResponseDto> subjectDtos = subjectPage.getContent().stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();

        return SubjectListResponseDto.of(subjectDtos, subjectPage.getTotalElements());
    }

    private Specification<Subject> buildSpecification(SubjectSearchRequestDto request) {
        return Specification.where(SubjectSpecification.hasInstituteId(request.getInstituteId()))
                .and(SubjectSpecification.hasCourseId(request.getCourseId()))
                .and(SubjectSpecification.hasTextContaining(request.getSearchText()))
                .and(SubjectSpecification.hasNameContaining(request.getName()))
                .and(SubjectSpecification.hasCodeContaining(request.getCode()))
                .and(SubjectSpecification.hasDescriptionContaining(request.getDescription()))
                .and(SubjectSpecification.hasDurationRange(request.getMinDuration(), request.getMaxDuration()))
                .and(SubjectSpecification.hasCreditsRange(request.getMinCredits(), request.getMaxCredits()))
                .and(SubjectSpecification.hasPrerequisitesContaining(request.getPrerequisites()))
                .and(SubjectSpecification.isActive(request.getActive()))
                .and(SubjectSpecification.isNotDeleted())
                .and(request.getHasChapters() != null && request.getHasChapters() ? 
                     SubjectSpecification.hasChapters() : null)
                .and(SubjectSpecification.hasMinimumChapters(request.getMinChapters()))
                .and(SubjectSpecification.hasMaximumChapters(request.getMaxChapters()))
                .and(SubjectSpecification.createdAfter(request.getCreatedAfter()))
                .and(SubjectSpecification.createdBefore(request.getCreatedBefore()))
                .and(SubjectSpecification.createdBy(request.getCreatedBy()));
    }

    private Pageable createPageable(SubjectSearchRequestDto request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        
        return PageRequest.of(page, size, sort);
    }

}


