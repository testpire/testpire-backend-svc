package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateCourseRequestDto;
import com.testpire.testpire.dto.request.CourseSearchRequestDto;
import com.testpire.testpire.dto.request.UpdateCourseRequestDto;
import com.testpire.testpire.dto.response.CourseListResponseDto;
import com.testpire.testpire.dto.response.CourseResponseDto;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.repository.CourseRepository;
import com.testpire.testpire.repository.specification.CourseSpecification;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public CourseResponseDto createCourse(CreateCourseRequestDto request) {
        log.info("Creating course: {} for institute: {}", request.name(), request.instituteId());

        // Check if course code already exists in the institute
        if (courseRepository.existsByCodeAndInstituteId(request.code(), request.instituteId())) {
            throw new IllegalArgumentException("Course with code " + request.code() + " already exists in this institute");
        }

        Course course = Course.builder()
                .name(request.name())
                .description(request.description())
                .code(request.code())
                .instituteId(request.instituteId())
                .duration(request.duration())
                .level(request.level())
                .prerequisites(request.prerequisites())
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());
        return CourseResponseDto.fromEntity(savedCourse);
    }

    @Transactional
    public CourseResponseDto updateCourse(Long id, UpdateCourseRequestDto request) {
        log.info("Updating course with ID: {}", id);

        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));

        // Check if new code conflicts with existing courses in the same institute
        if (request.code() != null && !request.code().equals(existingCourse.getCode()) &&
            courseRepository.existsByCodeAndInstituteId(request.code(), existingCourse.getInstituteId())) {
            throw new IllegalArgumentException("Course with code " + request.code() + " already exists in this institute");
        }

        Optional.ofNullable(request.name()).ifPresent(existingCourse::setName);
        Optional.ofNullable(request.description()).ifPresent(existingCourse::setDescription);
        Optional.ofNullable(request.code()).ifPresent(existingCourse::setCode);
        Optional.ofNullable(request.duration()).ifPresent(existingCourse::setDuration);
        Optional.ofNullable(request.level()).ifPresent(existingCourse::setLevel);
        Optional.ofNullable(request.prerequisites()).ifPresent(existingCourse::setPrerequisites);
        Optional.ofNullable(request.active()).ifPresent(existingCourse::setActive);
        existingCourse.setUpdatedBy(RequestUtils.getCurrentUsername());

        Course updatedCourse = courseRepository.save(existingCourse);
        log.info("Course updated successfully with ID: {}", updatedCourse.getId());
        return CourseResponseDto.fromEntity(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));

        course.setActive(false);
        course.setUpdatedBy(RequestUtils.getCurrentUsername());
        courseRepository.save(course);
        log.info("Course deactivated successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));
        return CourseResponseDto.fromEntity(course);
    }

    @Transactional(readOnly = true)
    public CourseResponseDto getCourseByCode(String code, Long instituteId) {
        Course course = courseRepository.findByCodeAndInstituteId(code, instituteId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with code: " + code));
        return CourseResponseDto.fromEntity(course);
    }


    @Transactional(readOnly = true)
    public CourseListResponseDto searchCoursesWithSpecification(CourseSearchRequestDto request) {
        log.info("Searching courses with specification: {}", request);

        // Build specification
        Specification<Course> spec = buildSpecification(request);

        // Create pageable
        Pageable pageable = createPageable(request);

        // Execute search
        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<CourseResponseDto> courseDtos = coursePage.getContent().stream()
                .map(CourseResponseDto::fromEntity)
                .toList();

        return CourseListResponseDto.of(courseDtos, coursePage.getTotalElements());
    }

    private Specification<Course> buildSpecification(CourseSearchRequestDto request) {
        return Specification.where(CourseSpecification.hasInstituteId(request.getInstituteId()))
                .and(CourseSpecification.hasTextContaining(request.getSearchText()))
                .and(CourseSpecification.hasNameContaining(request.getName()))
                .and(CourseSpecification.hasCodeContaining(request.getCode()))
                .and(CourseSpecification.hasDescriptionContaining(request.getDescription()))
                .and(CourseSpecification.hasDurationRange(request.getMinDuration(), request.getMaxDuration()))
                .and(CourseSpecification.hasLevel(request.getLevel()))
                .and(CourseSpecification.hasPrerequisitesContaining(request.getPrerequisites()))
                .and(CourseSpecification.isActive(request.getActive()))
                .and(CourseSpecification.isNotDeleted())
                .and(request.getHasSubjects() != null && request.getHasSubjects() ? 
                     CourseSpecification.hasSubjects() : null)
                .and(CourseSpecification.hasMinimumSubjects(request.getMinSubjects()))
                .and(CourseSpecification.hasMaximumSubjects(request.getMaxSubjects()))
                .and(CourseSpecification.createdAfter(request.getCreatedAfter()))
                .and(CourseSpecification.createdBefore(request.getCreatedBefore()))
                .and(CourseSpecification.createdBy(request.getCreatedBy()));
    }

    private Pageable createPageable(CourseSearchRequestDto request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        
        return PageRequest.of(page, size, sort);
    }
}


