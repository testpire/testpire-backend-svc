package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateSubjectRequestDto;
import com.testpire.testpire.dto.request.UpdateSubjectRequestDto;
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
    public SubjectListResponseDto getSubjectsByInstitute(Long instituteId) {
        List<Subject> subjects = subjectRepository.findByInstituteIdAndActiveTrue(instituteId);
        List<SubjectResponseDto> subjectDtos = subjects.stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();
        return SubjectListResponseDto.of(subjectDtos);
    }

    @Transactional(readOnly = true)
    public SubjectListResponseDto getSubjectsByCourse(Long courseId, Long instituteId) {
        List<Subject> subjects = subjectRepository.findByCourseIdAndInstituteIdAndActiveTrue(courseId, instituteId);
        List<SubjectResponseDto> subjectDtos = subjects.stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();
        return SubjectListResponseDto.of(subjectDtos);
    }

    @Transactional(readOnly = true)
    public SubjectListResponseDto searchSubjects(Long instituteId, String query) {
        List<Subject> subjects = subjectRepository.findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(
                instituteId, query, instituteId, query);
        List<SubjectResponseDto> subjectDtos = subjects.stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();
        return SubjectListResponseDto.of(subjectDtos);
    }

    @Transactional(readOnly = true)
    public SubjectListResponseDto getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectResponseDto> subjectDtos = subjects.stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();
        return SubjectListResponseDto.of(subjectDtos);
    }
}

