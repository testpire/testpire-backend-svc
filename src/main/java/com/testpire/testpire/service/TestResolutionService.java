package com.testpire.testpire.service;

import com.testpire.testpire.dto.response.AvailableTestResponseDto;
import com.testpire.testpire.entity.StudentEnrollment;
import com.testpire.testpire.entity.Test;
import com.testpire.testpire.entity.TestAssignment;
import com.testpire.testpire.entity.TestAttempt;
import com.testpire.testpire.enums.AttemptStatus;
import com.testpire.testpire.enums.TestStatus;
import com.testpire.testpire.repository.StudentEnrollmentRepository;
import com.testpire.testpire.repository.TestAssignmentRepository;
import com.testpire.testpire.repository.TestAttemptRepository;
import com.testpire.testpire.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The dynamic-resolution core: computes which tests a student may take by joining the student's current
 * {@code student_enrollments} (course + batch) against active {@link TestAssignment}s — without any
 * materialized per-student rows. A student who enrolls after a course/batch assignment is created
 * inherits it automatically here.
 *
 * <p>A test is available iff: it is PUBLISHED and active; "now" is inside the effective availability
 * window (the test window narrowed by the qualifying assignment's window); and the student has either
 * an in-progress attempt to resume or attempts remaining below {@code maxAttempts}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestResolutionService {

    // Sentinel so an empty target set never produces an empty SQL IN (...) list.
    private static final Long NO_MATCH = -1L;

    private final StudentEnrollmentRepository enrollmentRepository;
    private final TestAssignmentRepository assignmentRepository;
    private final TestRepository testRepository;
    private final TestAttemptRepository attemptRepository;

    /**
     * Active assignments (across all tests) that reach this student, keyed by testId. When more than
     * one assignment reaches the same test, the one with the widest effective window wins for display;
     * any qualifying assignment is sufficient for eligibility.
     */
    @Transactional(readOnly = true)
    public Map<Long, TestAssignment> resolveAssignmentsForStudent(Long studentUserId, Long instituteId) {
        List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentUserId(studentUserId);
        Set<Long> courseIds = enrollments.stream().map(StudentEnrollment::getCourseId).collect(Collectors.toSet());
        Set<Long> batchIds = enrollments.stream().map(StudentEnrollment::getBatchId).collect(Collectors.toSet());
        if (courseIds.isEmpty()) courseIds = Set.of(NO_MATCH);
        if (batchIds.isEmpty()) batchIds = Set.of(NO_MATCH);

        List<TestAssignment> assignments = assignmentRepository.findActiveForStudentTargets(
                instituteId, courseIds, batchIds, studentUserId);

        Map<Long, TestAssignment> byTest = new LinkedHashMap<>();
        for (TestAssignment a : assignments) {
            byTest.merge(a.getTestId(), a, (existing, candidate) ->
                    // Prefer the assignment with the later (or absent) expiry for display purposes.
                    isWiderUntil(candidate.getAvailableUntil(), existing.getAvailableUntil()) ? candidate : existing);
        }
        return byTest;
    }

    /** Builds the student's list of currently-takeable tests. */
    @Transactional(readOnly = true)
    public List<AvailableTestResponseDto> getAvailableTestsForStudent(Long studentUserId, Long instituteId) {
        Map<Long, TestAssignment> byTest = resolveAssignmentsForStudent(studentUserId, instituteId);
        if (byTest.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        List<AvailableTestResponseDto> result = new ArrayList<>();
        for (Test test : testRepository.findAllById(byTest.keySet())) {
            if (test.getStatus() != TestStatus.PUBLISHED || !test.isActive()) {
                continue;
            }
            TestAssignment assignment = byTest.get(test.getId());
            LocalDateTime from = laterOf(test.getAvailableFrom(), assignment.getAvailableFrom());
            LocalDateTime until = earlierOf(test.getAvailableUntil(), assignment.getAvailableUntil());
            if (from != null && now.isBefore(from)) continue;   // not open yet
            if (until != null && now.isAfter(until)) continue;   // expired

            List<TestAttempt> attempts = attemptRepository.findByTestIdAndStudentUserId(test.getId(), studentUserId);
            TestAttempt inProgress = attempts.stream()
                    .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                    .findFirst().orElse(null);
            int used = attempts.size();
            boolean hasAttemptsLeft = used < test.getMaxAttempts();
            if (inProgress == null && !hasAttemptsLeft) {
                continue; // exhausted, nothing to resume
            }
            result.add(new AvailableTestResponseDto(
                    test.getId(), test.getTitle(), test.getDescription(), test.getTotalMarks(),
                    test.getDurationMinutes(), test.getMaxAttempts(), used, from, until,
                    inProgress != null ? inProgress.getId() : null));
        }
        return result;
    }

    /**
     * Returns the assignment through which {@code studentUserId} qualifies for {@code test}, validating
     * the effective availability window. Throws if the student is not eligible or the window is closed.
     */
    @Transactional(readOnly = true)
    public TestAssignment requireEligibility(Test test, Long studentUserId) {
        TestAssignment assignment = resolveAssignmentsForStudent(studentUserId, test.getInstituteId()).get(test.getId());
        if (assignment == null) {
            throw new IllegalStateException("You are not assigned this test");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = laterOf(test.getAvailableFrom(), assignment.getAvailableFrom());
        LocalDateTime until = earlierOf(test.getAvailableUntil(), assignment.getAvailableUntil());
        if (from != null && now.isBefore(from)) {
            throw new IllegalStateException("This test is not open yet");
        }
        if (until != null && now.isAfter(until)) {
            throw new IllegalStateException("This test has expired");
        }
        return assignment;
    }

    /** Effective expiry of the test for this student (test window narrowed by assignment window). */
    public LocalDateTime effectiveUntil(Test test, TestAssignment assignment) {
        return earlierOf(test.getAvailableUntil(), assignment == null ? null : assignment.getAvailableUntil());
    }

    private static boolean isWiderUntil(LocalDateTime candidate, LocalDateTime current) {
        if (candidate == null) return true;          // null = no expiry = widest
        if (current == null) return false;
        return candidate.isAfter(current);
    }

    private static LocalDateTime laterOf(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime earlierOf(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }
}
