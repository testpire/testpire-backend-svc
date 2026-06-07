package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.EnrollmentRequestDto;
import com.testpire.testpire.dto.response.EnrollmentResponseDto;
import com.testpire.testpire.entity.Batch;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.entity.StudentEnrollment;
import com.testpire.testpire.repository.BatchRepository;
import com.testpire.testpire.repository.CourseRepository;
import com.testpire.testpire.repository.StudentEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages a student's course+batch enrollments (the {@code student_enrollments} source of truth).
 *
 * <p>Every enrollment is validated: the course must exist in the student's institute, the batch must
 * exist in that institute and belong to that course. A student may have at most one enrollment per
 * course (DB-enforced unique), so a payload may not list the same course twice.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentEnrollmentService {

    private final StudentEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;

    /**
     * Replaces the student's full enrollment set so it matches {@code requested}: courses present in
     * the request are inserted (or re-pointed to a new batch); enrollments for courses absent from
     * the request are removed. Pass an empty list to clear all enrollments. A {@code null} list
     * should be handled by the caller (means "leave unchanged") and must not reach here.
     */
    @Transactional
    public void syncEnrollments(Long studentUserId, Long instituteId,
                                List<EnrollmentRequestDto> requested, String actor) {
        Map<Long, Long> desired = toValidatedCourseBatchMap(requested, instituteId);

        Map<Long, StudentEnrollment> existing = new HashMap<>();
        for (StudentEnrollment e : enrollmentRepository.findByStudentUserId(studentUserId)) {
            existing.put(e.getCourseId(), e);
        }

        // Insert new / re-point changed.
        for (Map.Entry<Long, Long> want : desired.entrySet()) {
            Long courseId = want.getKey();
            Long batchId = want.getValue();
            StudentEnrollment current = existing.get(courseId);
            if (current == null) {
                enrollmentRepository.save(StudentEnrollment.builder()
                        .studentUserId(studentUserId)
                        .courseId(courseId)
                        .batchId(batchId)
                        .instituteId(instituteId)
                        .createdBy(actor)
                        .build());
            } else if (!current.getBatchId().equals(batchId)) {
                current.setBatchId(batchId);
                current.setUpdatedBy(actor);
                enrollmentRepository.save(current);
            }
        }

        // Remove enrollments for courses no longer requested.
        for (StudentEnrollment e : existing.values()) {
            if (!desired.containsKey(e.getCourseId())) {
                enrollmentRepository.delete(e);
            }
        }
        log.info("Synced {} enrollment(s) for student {}", desired.size(), studentUserId);
    }

    /**
     * Adds a single course+batch enrollment (used by lead conversion). No-op-safe: if the student is
     * already enrolled in the course, the existing enrollment is left as-is.
     */
    @Transactional
    public void addEnrollment(Long studentUserId, Long instituteId, Long courseId, Long batchId, String actor) {
        validateCourseBatch(courseId, batchId, instituteId);
        if (enrollmentRepository.existsByStudentUserIdAndCourseId(studentUserId, courseId)) {
            log.info("Student {} already enrolled in course {}; skipping add", studentUserId, courseId);
            return;
        }
        enrollmentRepository.save(StudentEnrollment.builder()
                .studentUserId(studentUserId)
                .courseId(courseId)
                .batchId(batchId)
                .instituteId(instituteId)
                .createdBy(actor)
                .build());
        log.info("Enrolled student {} into course {} / batch {}", studentUserId, courseId, batchId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDto> getEnrollments(Long studentUserId) {
        return enrollmentRepository.findByStudentUserId(studentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Bulk variant of {@link #getEnrollments} for list/search views: resolves enrollments for many
     * students in a fixed 3 queries (enrollments + course names + batch names) regardless of student
     * count, instead of the per-student N+1 that {@link #getEnrollments} incurs when called in a loop.
     * Returns a map keyed by student user id; students with no enrollments are simply absent.
     */
    @Transactional(readOnly = true)
    public Map<Long, List<EnrollmentResponseDto>> getEnrollmentsForStudents(Collection<Long> studentUserIds) {
        if (studentUserIds == null || studentUserIds.isEmpty()) {
            return Map.of();
        }
        List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentUserIdIn(studentUserIds);
        if (enrollments.isEmpty()) {
            return Map.of();
        }

        Set<Long> courseIds = enrollments.stream().map(StudentEnrollment::getCourseId).collect(Collectors.toSet());
        Set<Long> batchIds = enrollments.stream().map(StudentEnrollment::getBatchId).collect(Collectors.toSet());
        Map<Long, String> courseNames = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));
        Map<Long, String> batchNames = batchRepository.findAllById(batchIds).stream()
                .collect(Collectors.toMap(Batch::getId, Batch::getName));

        Map<Long, List<EnrollmentResponseDto>> byStudent = new HashMap<>();
        for (StudentEnrollment e : enrollments) {
            byStudent.computeIfAbsent(e.getStudentUserId(), k -> new ArrayList<>())
                    .add(new EnrollmentResponseDto(e.getId(), e.getCourseId(),
                            courseNames.get(e.getCourseId()), e.getBatchId(), batchNames.get(e.getBatchId())));
        }
        return byStudent;
    }

    /** Validates and de-duplicates the requested enrollments into a courseId -> batchId map. */
    private Map<Long, Long> toValidatedCourseBatchMap(List<EnrollmentRequestDto> requested, Long instituteId) {
        Map<Long, Long> desired = new LinkedHashMap<>();
        if (requested == null) {
            return desired;
        }
        for (EnrollmentRequestDto e : requested) {
            if (desired.containsKey(e.courseId())) {
                throw new IllegalArgumentException(
                        "Duplicate enrollment for course ID " + e.courseId() + " in the request");
            }
            validateCourseBatch(e.courseId(), e.batchId(), instituteId);
            desired.put(e.courseId(), e.batchId());
        }
        return desired;
    }

    private void validateCourseBatch(Long courseId, Long batchId, Long instituteId) {
        if (courseRepository.findByIdAndInstituteId(courseId, instituteId).isEmpty()) {
            throw new IllegalArgumentException("Course not found in this institute with ID: " + courseId);
        }
        Batch batch = batchRepository.findByIdAndInstituteId(batchId, instituteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Batch not found in this institute with ID: " + batchId));
        if (!batch.getCourseId().equals(courseId)) {
            throw new IllegalArgumentException(
                    "Batch " + batchId + " does not belong to course " + courseId);
        }
    }

    private EnrollmentResponseDto toResponse(StudentEnrollment e) {
        String courseName = courseRepository.findById(e.getCourseId()).map(Course::getName).orElse(null);
        String batchName = batchRepository.findById(e.getBatchId()).map(Batch::getName).orElse(null);
        return new EnrollmentResponseDto(e.getId(), e.getCourseId(), courseName, e.getBatchId(), batchName);
    }
}
