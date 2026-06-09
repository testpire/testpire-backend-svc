package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.AssignTestRequestDto;
import com.testpire.testpire.dto.response.TestAssignmentResponseDto;
import com.testpire.testpire.entity.Batch;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.entity.Test;
import com.testpire.testpire.entity.TestAssignment;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.AssignmentTargetType;
import com.testpire.testpire.enums.TestStatus;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.repository.BatchRepository;
import com.testpire.testpire.repository.CourseRepository;
import com.testpire.testpire.repository.TestAssignmentRepository;
import com.testpire.testpire.repository.UserRepository;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Assigns a published test to a course / batch / student (logical, non-materialized). The target must
 * exist in the test's institute. Listing/unassigning is institute-scoped. The actual fan-out to
 * students is resolved at read time by {@link TestResolutionService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestAssignmentService {

    private final TestAssignmentRepository assignmentRepository;
    private final TestService testService;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Transactional
    public TestAssignmentResponseDto assign(Long testId, AssignTestRequestDto request) {
        log.debug("Assigning test {} to {} {} (availableFrom={}, availableUntil={})",
                testId, request.targetType(), request.targetId(), request.availableFrom(), request.availableUntil());
        Test test = testService.findScoped(testId);
        log.debug("Test {} status={}", testId, test.getStatus());
        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new IllegalStateException("Only a PUBLISHED test can be assigned (current: " + test.getStatus() + ")");
        }
        validateWindow(request.availableFrom(), request.availableUntil());

        String targetName = validateTargetAndGetName(request.targetType(), request.targetId(), test.getInstituteId());
        log.debug("Assignment target validated: {} {} = '{}'", request.targetType(), request.targetId(), targetName);

        if (assignmentRepository.existsByTestIdAndTargetTypeAndTargetId(
                testId, request.targetType(), request.targetId())) {
            throw new IllegalArgumentException("This test is already assigned to that "
                    + request.targetType().name().toLowerCase());
        }

        TestAssignment assignment = TestAssignment.builder()
                .testId(testId)
                .instituteId(test.getInstituteId())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .availableFrom(request.availableFrom())
                .availableUntil(request.availableUntil())
                .assignedBy(RequestUtils.getCurrentUsername())
                .build();
        TestAssignment saved = assignmentRepository.save(assignment);
        log.info("Test {} assigned to {} {} (institute {})",
                testId, request.targetType(), request.targetId(), test.getInstituteId());
        return TestAssignmentResponseDto.fromEntity(saved, targetName);
    }

    @Transactional(readOnly = true)
    public List<TestAssignmentResponseDto> listAssignments(Long testId) {
        log.debug("Listing assignments for test {}", testId);
        Test test = testService.findScoped(testId); // institute scoping + existence check
        List<TestAssignment> assignments = assignmentRepository.findByTestId(test.getId());
        log.debug("Test {} has {} assignment(s)", testId, assignments.size());
        return assignments.stream()
                .map(a -> TestAssignmentResponseDto.fromEntity(a, resolveTargetName(a.getTargetType(), a.getTargetId())))
                .toList();
    }

    @Transactional
    public void unassign(Long testId, Long assignmentId) {
        log.debug("Unassigning assignment {} from test {}", assignmentId, testId);
        Test test = testService.findScoped(testId); // institute scoping
        TestAssignment assignment = assignmentRepository.findById(assignmentId)
                .filter(a -> a.getTestId().equals(test.getId()))
                .filter(a -> a.getInstituteId().equals(test.getInstituteId()))
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));
        log.debug("Found assignment {}: {} {} for test {}", assignmentId, assignment.getTargetType(), assignment.getTargetId(), testId);
        assignmentRepository.delete(assignment);
        log.info("Assignment {} removed from test {}", assignmentId, testId);
    }

    // --- helpers -----------------------------------------------------------

    private String validateTargetAndGetName(AssignmentTargetType type, Long targetId, Long instituteId) {
        switch (type) {
            case COURSE -> {
                Course c = courseRepository.findByIdAndInstituteId(targetId, instituteId)
                        .orElseThrow(() -> new IllegalArgumentException("Course not found in this institute: " + targetId));
                return c.getName();
            }
            case BATCH -> {
                Batch b = batchRepository.findByIdAndInstituteId(targetId, instituteId)
                        .orElseThrow(() -> new IllegalArgumentException("Batch not found in this institute: " + targetId));
                return b.getName();
            }
            case STUDENT -> {
                User u = userRepository.findById(targetId)
                        .filter(user -> instituteId.equals(user.getInstituteId()))
                        .filter(user -> user.getRole() == UserRole.STUDENT)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found in this institute: " + targetId));
                return u.getUsername();
            }
            default -> throw new IllegalArgumentException("Unsupported target type: " + type);
        }
    }

    /** Best-effort name for display; returns null rather than throwing if the target was since removed. */
    private String resolveTargetName(AssignmentTargetType type, Long targetId) {
        return switch (type) {
            case COURSE -> courseRepository.findById(targetId).map(Course::getName).orElse(null);
            case BATCH -> batchRepository.findById(targetId).map(Batch::getName).orElse(null);
            case STUDENT -> userRepository.findById(targetId).map(User::getUsername).orElse(null);
        };
    }

    private void validateWindow(java.time.Instant from, java.time.Instant until) {
        if (from != null && until != null && until.isBefore(from)) {
            throw new IllegalArgumentException("availableUntil cannot be before availableFrom");
        }
    }
}
