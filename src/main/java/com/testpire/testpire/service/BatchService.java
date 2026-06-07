package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateBatchRequestDto;
import com.testpire.testpire.dto.request.UpdateBatchRequestDto;
import com.testpire.testpire.dto.response.BatchResponseDto;
import com.testpire.testpire.entity.Batch;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.repository.BatchRepository;
import com.testpire.testpire.repository.CourseRepository;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CRUD for batches (cohorts) under a course. Multi-tenancy: a non-SUPER_ADMIN caller is scoped to
 * their JWT institute; SUPER_ADMIN may target any institute (resolved via {@link RequestUtils}).
 * Every batch is validated to belong to an existing course in the same institute, and its name/code
 * are unique within that course.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public BatchResponseDto createBatch(CreateBatchRequestDto request) {
        Long instituteId = RequestUtils.resolveInstituteId(request.instituteId());
        if (instituteId == null) {
            throw new IllegalArgumentException("Institute ID is required");
        }

        // Parent course must exist in this institute.
        Course course = courseRepository.findByIdAndInstituteId(request.courseId(), instituteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course not found in this institute with ID: " + request.courseId()));

        if (batchRepository.existsByCourseIdAndNameIgnoreCase(request.courseId(), request.name())) {
            throw new IllegalArgumentException(
                    "A batch named \"" + request.name() + "\" already exists in this course");
        }
        if (request.code() != null && batchRepository.existsByCourseIdAndCode(request.courseId(), request.code())) {
            throw new IllegalArgumentException(
                    "A batch with code \"" + request.code() + "\" already exists in this course");
        }
        validateDates(request.startDate(), request.endDate());

        Batch batch = Batch.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .courseId(request.courseId())
                .instituteId(instituteId)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .capacity(request.capacity())
                .fee(request.fee()) // null = inherit the course fee
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        Batch saved = batchRepository.save(batch);
        log.info("Batch created with ID {} under course {} (institute {})",
                saved.getId(), saved.getCourseId(), instituteId);
        return BatchResponseDto.fromEntity(saved, course.getFee());
    }

    @Transactional
    public BatchResponseDto updateBatch(Long id, UpdateBatchRequestDto request) {
        Batch batch = findScoped(id);

        if (request.name() != null && !request.name().equalsIgnoreCase(batch.getName())
                && batchRepository.existsByCourseIdAndNameIgnoreCase(batch.getCourseId(), request.name())) {
            throw new IllegalArgumentException(
                    "A batch named \"" + request.name() + "\" already exists in this course");
        }
        if (request.code() != null && !request.code().equals(batch.getCode())
                && batchRepository.existsByCourseIdAndCode(batch.getCourseId(), request.code())) {
            throw new IllegalArgumentException(
                    "A batch with code \"" + request.code() + "\" already exists in this course");
        }

        Optional.ofNullable(request.name()).ifPresent(batch::setName);
        Optional.ofNullable(request.code()).ifPresent(batch::setCode);
        Optional.ofNullable(request.description()).ifPresent(batch::setDescription);
        Optional.ofNullable(request.startDate()).ifPresent(batch::setStartDate);
        Optional.ofNullable(request.endDate()).ifPresent(batch::setEndDate);
        Optional.ofNullable(request.capacity()).ifPresent(batch::setCapacity);
        Optional.ofNullable(request.active()).ifPresent(batch::setActive);
        applyFeeChange(batch, request.fee(), request.inheritFee());
        validateDates(batch.getStartDate(), batch.getEndDate());
        batch.setUpdatedBy(RequestUtils.getCurrentUsername());

        Batch saved = batchRepository.save(batch);
        log.info("Batch updated with ID: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Applies a fee change to a batch: {@code inheritFee=true} clears the override (back to inheriting
     * the course fee); otherwise a non-null {@code fee} sets/replaces the override. Sending both is an
     * error, and a null {@code fee} without {@code inheritFee} leaves the current fee unchanged.
     */
    private void applyFeeChange(Batch batch, BigDecimal fee, Boolean inheritFee) {
        if (Boolean.TRUE.equals(inheritFee)) {
            if (fee != null) {
                throw new IllegalArgumentException(
                        "Cannot set a fee and inheritFee=true in the same request");
            }
            batch.setFee(null);
        } else if (fee != null) {
            batch.setFee(fee);
        }
    }

    @Transactional
    public void deleteBatch(Long id) {
        Batch batch = findScoped(id);
        // Soft delete (mirrors Course): @SQLDelete flips the deleted flag.
        batchRepository.delete(batch);
        log.info("Batch soft-deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public BatchResponseDto getBatchById(Long id) {
        return toResponse(findScoped(id));
    }

    /** Builds a response, resolving the batch's effective fee against its parent course's fee. */
    private BatchResponseDto toResponse(Batch batch) {
        BigDecimal courseFee = courseRepository.findById(batch.getCourseId())
                .map(Course::getFee)
                .orElse(null);
        return BatchResponseDto.fromEntity(batch, courseFee);
    }

    @Transactional(readOnly = true)
    public List<BatchResponseDto> getBatchesByCourse(Long courseId) {
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        Course course;
        List<Batch> batches;
        // Confirm the course is visible to the caller before listing its batches.
        if (instituteId != null) {
            course = courseRepository.findByIdAndInstituteId(courseId, instituteId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
            batches = batchRepository.findByCourseIdAndInstituteId(courseId, instituteId);
        } else {
            course = courseRepository.findById(courseId).orElse(null);
            batches = batchRepository.findByCourseId(courseId);
        }
        // All batches share the same parent course, so resolve its fee once.
        BigDecimal courseFee = course != null ? course.getFee() : null;
        return batches.stream()
                .map(b -> BatchResponseDto.fromEntity(b, courseFee))
                .toList();
    }

    private void validateDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Batch end date cannot be before its start date");
        }
    }

    /**
     * Loads a batch scoped to the caller's institute. Non-SUPER_ADMIN users are restricted to their
     * JWT institute (a batch in another institute reads as not-found — do not leak cross-tenant
     * existence). SUPER_ADMIN (null instituteId) uses the unscoped lookup.
     */
    private Batch findScoped(Long id) {
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        return (instituteId != null
                ? batchRepository.findByIdAndInstituteId(id, instituteId)
                : batchRepository.findById(id))
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with ID: " + id));
    }
}
