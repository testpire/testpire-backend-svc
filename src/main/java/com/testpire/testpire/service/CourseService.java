package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateCourseRequestDto;
import com.testpire.testpire.dto.request.UpdateCourseRequestDto;
import com.testpire.testpire.dto.response.CourseListResponseDto;
import com.testpire.testpire.dto.response.CourseResponseDto;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.repository.CourseRepository;
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
    public CourseListResponseDto getCoursesByInstitute(Long instituteId) {
        List<Course> courses = courseRepository.findByInstituteIdAndActiveTrue(instituteId);
        List<CourseResponseDto> courseDtos = courses.stream()
                .map(CourseResponseDto::fromEntity)
                .toList();
        return CourseListResponseDto.of(courseDtos);
    }

    @Transactional(readOnly = true)
    public CourseListResponseDto searchCourses(Long instituteId, String query) {
        List<Course> courses = courseRepository.findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(
                instituteId, query, instituteId, query);
        List<CourseResponseDto> courseDtos = courses.stream()
                .map(CourseResponseDto::fromEntity)
                .toList();
        return CourseListResponseDto.of(courseDtos);
    }

    @Transactional(readOnly = true)
    public CourseListResponseDto getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        List<CourseResponseDto> courseDtos = courses.stream()
                .map(CourseResponseDto::fromEntity)
                .toList();
        return CourseListResponseDto.of(courseDtos);
    }
}

