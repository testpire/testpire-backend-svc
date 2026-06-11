package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.SubmitAnswerRequestDto;
import com.testpire.testpire.dto.response.AttemptQuestionResponseDto;
import com.testpire.testpire.dto.response.AttemptSummaryResponseDto;
import com.testpire.testpire.dto.response.TestAttemptResponseDto;
import com.testpire.testpire.dto.response.TestResultResponseDto;
import com.testpire.testpire.entity.Option;
import com.testpire.testpire.entity.Question;
import com.testpire.testpire.entity.Test;
import com.testpire.testpire.entity.TestAssignment;
import com.testpire.testpire.entity.TestAttempt;
import com.testpire.testpire.entity.TestAttemptAnswer;
import com.testpire.testpire.entity.TestQuestion;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.AttemptStatus;
import com.testpire.testpire.enums.TestStatus;
import com.testpire.testpire.repository.OptionRepository;
import com.testpire.testpire.repository.TestAttemptAnswerRepository;
import com.testpire.testpire.repository.TestAttemptRepository;
import com.testpire.testpire.repository.TestQuestionRepository;
import com.testpire.testpire.repository.TestRepository;
import com.testpire.testpire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drives a student's test attempt: start (eligibility + timer), save answers, submit, and auto-grade
 * objective questions. Timing is server-enforced — an attempt carries a hard {@code expiresAt}, and
 * any read of an over-time IN_PROGRESS attempt finalizes it (lazy expiry; no scheduler in v1).
 *
 * <p>Grading is all-or-nothing per question: an answer is correct iff the set of selected options
 * exactly equals the set of correct options. Correct answers earn the effective marks; wrong/answered
 * questions lose the effective negative marks when the test enables negative marking; blanks score 0.
 * The attempt score is floored at 0.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestAttemptService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestAttemptRepository attemptRepository;
    private final TestAttemptAnswerRepository answerRepository;
    private final OptionRepository optionRepository;
    private final TestResolutionService resolutionService;
    private final TestService testService;
    private final UserRepository userRepository;

    // --- Start / resume ----------------------------------------------------

    @Transactional
    public TestAttemptResponseDto startAttempt(Long testId, Long studentUserId, Long instituteId) {
        log.debug("startAttempt: student={}, test={}, institute={}", studentUserId, testId, instituteId);
        Test test = loadStudentTest(testId, instituteId);
        log.debug("Test {} status={}, maxAttempts={}", testId, test.getStatus(), test.getMaxAttempts());
        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new IllegalStateException("This test is not open for attempts");
        }
        TestAssignment assignment = resolutionService.requireEligibility(test, studentUserId);
        log.debug("Student {} eligible via assignment {} (targetType={}, targetId={})",
                studentUserId, assignment.getId(), assignment.getTargetType(), assignment.getTargetId());

        List<TestAttempt> existing = attemptRepository.findByTestIdAndStudentUserId(testId, studentUserId);
        log.debug("Student {} has {} existing attempt(s) on test {}", studentUserId, existing.size(), testId);
        // Finalize any stale in-progress attempt first so attempt accounting is accurate.
        for (TestAttempt a : existing) {
            finalizeIfExpired(a, test);
        }
        TestAttempt inProgress = existing.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                .findFirst().orElse(null);
        if (inProgress != null) {
            log.debug("Resuming in-progress attempt {} for student {} on test {}", inProgress.getId(), studentUserId, testId);
            return buildAttemptResponse(inProgress, test); // resume
        }
        if (existing.size() >= test.getMaxAttempts()) {
            throw new IllegalStateException("No attempts remaining for this test");
        }
        log.debug("Creating new attempt for student {} on test {} (attempt #{} of {})",
                studentUserId, testId, existing.size() + 1, test.getMaxAttempts());

        Instant now = Instant.now();
        Instant durationDeadline = test.getDurationMinutes() != null
                ? now.plus(test.getDurationMinutes(), ChronoUnit.MINUTES) : null;
        Instant expiresAt = earlierOf(durationDeadline, resolutionService.effectiveUntil(test, assignment));

        TestAttempt attempt = TestAttempt.builder()
                .testId(testId)
                .assignmentId(assignment.getId())
                .studentUserId(studentUserId)
                .instituteId(test.getInstituteId())
                .status(AttemptStatus.IN_PROGRESS)
                .attemptNumber(existing.size() + 1)
                .startedAt(now)
                .expiresAt(expiresAt)
                .maxScore(test.getTotalMarks())
                .build();
        TestAttempt saved = attemptRepository.save(attempt);
        log.info("Student {} started attempt {} on test {} (expires {})",
                studentUserId, saved.getId(), testId, expiresAt);
        return buildAttemptResponse(saved, test);
    }

    // --- Save a single answer ---------------------------------------------

    @Transactional
    public void saveAnswer(Long attemptId, Long studentUserId, SubmitAnswerRequestDto dto) {
        log.debug("saveAnswer: attempt={}, student={}, question={}, options={}",
                attemptId, studentUserId, dto.questionId(), dto.selectedOptionIds());
        TestAttempt attempt = loadOwnAttempt(attemptId, studentUserId);
        Test test = loadStudentTest(attempt.getTestId(), attempt.getInstituteId());
        if (finalizeIfExpired(attempt, test)) {
            throw new IllegalStateException("Time is up — this attempt has been submitted");
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("This attempt is no longer in progress");
        }
        upsertAnswer(attempt, test, dto);
        log.debug("Answer saved: attempt={}, question={}", attemptId, dto.questionId());
    }

    // --- Submit (+ grade) --------------------------------------------------

    @Transactional
    public TestAttemptResponseDto submit(Long attemptId, Long studentUserId, List<SubmitAnswerRequestDto> answers) {
        log.debug("submit: attempt={}, student={}, batched answers={}", attemptId, studentUserId,
                answers == null ? 0 : answers.size());
        TestAttempt attempt = loadOwnAttempt(attemptId, studentUserId);
        Test test = loadStudentTest(attempt.getTestId(), attempt.getInstituteId());

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            log.debug("Attempt {} already finalized (status={}) — returning idempotent response", attemptId, attempt.getStatus());
            return buildAttemptResponse(attempt, test); // already finalized — idempotent
        }
        if (finalizeIfExpired(attempt, test)) {
            log.debug("Attempt {} expired before explicit submit — returning auto-submitted response", attemptId);
            return buildAttemptResponse(attempt, test); // deadline passed; graded as auto-submitted
        }
        if (answers != null) {
            log.debug("Processing {} batched answer(s) for attempt {}", answers.size(), attemptId);
            for (SubmitAnswerRequestDto ans : answers) {
                upsertAnswer(attempt, test, ans);
            }
        }
        grade(attempt, test, AttemptStatus.GRADED, Instant.now());
        log.info("Student {} submitted attempt {} (score {}/{})",
                studentUserId, attemptId, attempt.getScore(), attempt.getMaxScore());
        return buildAttemptResponse(attempt, test);
    }

    // --- Read --------------------------------------------------------------

    @Transactional
    public TestAttemptResponseDto getAttempt(Long attemptId, Long studentUserId) {
        TestAttempt attempt = loadOwnAttempt(attemptId, studentUserId);
        Test test = loadStudentTest(attempt.getTestId(), attempt.getInstituteId());
        finalizeIfExpired(attempt, test);
        return buildAttemptResponse(attempt, test);
    }

    /**
     * Staff drill-down into one student's attempt (institute-scoped via {@link TestService#findScoped},
     * which honours the JWT institute and SUPER_ADMIN's {@code X-Institute-Id}). Unlike the student's
     * own view, correct answers and per-question marks are always revealed, independent of the test's
     * {@code showAnswers} flag (that flag only governs what the student sees). The attempt must belong
     * to {@code testId} — and the test to the staff member's institute — or it reads as not-found.
     */
    @Transactional
    public TestAttemptResponseDto getAttemptForStaff(Long testId, Long attemptId) {
        Test test = testService.findScoped(testId); // staff institute scoping + existence
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .filter(a -> a.getTestId().equals(testId))
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found with ID: " + attemptId));
        finalizeIfExpired(attempt, test);
        return buildAttemptResponse(attempt, test, true);
    }

    /**
     * The calling student's own attempt history (most recent first), backing the "Results" tab.
     * Includes completed/graded attempts regardless of whether the test's assignment window is still
     * open — unlike {@code available}, which only lists currently-open tests. Stale in-progress
     * attempts whose deadline has passed are finalized lazily so their scores are accurate. Attempts
     * are resolved by the JWT student id, so a student only ever sees their own.
     */
    @Transactional
    public List<AttemptSummaryResponseDto> listOwnAttempts(Long studentUserId) {
        log.debug("listOwnAttempts: student={}", studentUserId);
        List<TestAttempt> attempts = attemptRepository.findByStudentUserId(studentUserId);
        log.debug("Student {} has {} total attempt(s) across all tests", studentUserId, attempts.size());
        if (attempts.isEmpty()) {
            return List.of();
        }
        Set<Long> testIds = attempts.stream().map(TestAttempt::getTestId).collect(Collectors.toSet());
        Map<Long, Test> tests = testRepository.findAllById(testIds).stream()
                .collect(Collectors.toMap(Test::getId, t -> t));

        // Most recent first; startedAt is always set, so it orders cleanly across all statuses.
        attempts.sort((x, y) -> y.getStartedAt().compareTo(x.getStartedAt()));

        List<AttemptSummaryResponseDto> rows = new ArrayList<>();
        for (TestAttempt a : attempts) {
            Test test = tests.get(a.getTestId());
            if (test == null) {
                continue; // test hard-deleted out from under the attempt — skip rather than 500
            }
            finalizeIfExpired(a, test);
            rows.add(new AttemptSummaryResponseDto(
                    a.getId(), test.getId(), test.getTitle(), a.getStatus(),
                    a.getAttemptNumber(), a.getScore(), a.getMaxScore(), a.getPassed(), a.getSubmittedAt()));
        }
        return rows;
    }

    /**
     * Staff view of every student's marks for a test (institute-scoped via {@link TestService}). One
     * row per attempt (so multi-attempt tests list each try), ordered by student then attempt number.
     * Stale in-progress attempts are finalized lazily so their scores are accurate.
     */
    @Transactional
    public TestResultResponseDto getResults(Long testId) {
        log.debug("getResults: test={}", testId);
        Test test = testService.findScoped(testId); // staff institute scoping + existence
        List<TestAttempt> attempts = attemptRepository.findByTestId(testId);
        log.debug("Test {} has {} attempt(s) total", testId, attempts.size());
        for (TestAttempt a : attempts) {
            finalizeIfExpired(a, test);
        }
        Set<Long> studentIds = attempts.stream().map(TestAttempt::getStudentUserId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<TestResultResponseDto.StudentResult> rows = attempts.stream()
                .sorted((x, y) -> {
                    int c = x.getStudentUserId().compareTo(y.getStudentUserId());
                    return c != 0 ? c : x.getAttemptNumber().compareTo(y.getAttemptNumber());
                })
                .map(a -> {
                    User u = users.get(a.getStudentUserId());
                    String name = u == null ? null : (u.getFirstName() + " " + u.getLastName()).trim();
                    return new TestResultResponseDto.StudentResult(
                            a.getStudentUserId(),
                            u == null ? null : u.getUsername(),
                            name,
                            a.getId(), a.getAttemptNumber(), a.getStatus(),
                            a.getScore(), a.getMaxScore(), a.getPassed(), a.getSubmittedAt());
                })
                .toList();

        return new TestResultResponseDto(test.getId(), test.getTitle(), test.getTotalMarks(),
                test.getPassingMarks(), rows.size(), rows);
    }

    // --- Grading -----------------------------------------------------------

    /**
     * Finalizes an IN_PROGRESS attempt whose deadline has passed, grading whatever was saved.
     * @return true if the attempt was expired (and is now finalized), false otherwise.
     */
    private boolean finalizeIfExpired(TestAttempt attempt, Test test) {
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS
                && attempt.getExpiresAt() != null
                && Instant.now().isAfter(attempt.getExpiresAt())) {
            grade(attempt, test, AttemptStatus.AUTO_SUBMITTED, attempt.getExpiresAt());
            log.info("Attempt {} auto-submitted (deadline {})", attempt.getId(), attempt.getExpiresAt());
            return true;
        }
        return false;
    }

    private void grade(TestAttempt attempt, Test test, AttemptStatus finalStatus, Instant submittedAt) {
        List<TestQuestion> testQuestions = testQuestionRepository.findByTestIdOrderBySortOrderAsc(test.getId());
        List<TestAttemptAnswer> answers = answerRepository.findByAttemptId(attempt.getId());
        log.debug("Grading attempt {}: {} questions, {} saved answer(s), negativeMarking={}, status->{}",
                attempt.getId(), testQuestions.size(), answers.size(), test.isNegativeMarking(), finalStatus);
        Map<Long, TestAttemptAnswer> answerByQuestion = answers.stream()
                .collect(Collectors.toMap(TestAttemptAnswer::getQuestionId, a -> a, (a, b) -> a));

        BigDecimal total = BigDecimal.ZERO;
        for (TestQuestion tq : testQuestions) {
            Long questionId = tq.getQuestion().getId();
            TestAttemptAnswer answer = answerByQuestion.get(questionId);
            BigDecimal marks = TestService.effectiveMarks(tq);
            BigDecimal negative = TestService.effectiveNegativeMarks(tq);

            Set<Long> selected = answer == null ? Set.of() : parseOptionIds(answer.getSelectedOptionIds());
            Set<Long> correct = optionRepository.findCorrectOptionsByQuestionId(questionId).stream()
                    .map(Option::getId).collect(Collectors.toSet());

            GradedAnswer graded = gradeAnswer(selected, correct, marks, negative, test.isNegativeMarking());
            log.debug("  question={}: selected={}, correct={}, awarded={}, isCorrect={}",
                    questionId, selected, correct, graded.awarded(), graded.isCorrect());
            total = total.add(graded.awarded());

            if (answer != null) {
                answer.setIsCorrect(graded.isCorrect());
                answer.setMarksAwarded(graded.awarded());
                answerRepository.save(answer);
            }
        }
        // Floor the attempt score at zero (negative marking cannot drive the total below 0).
        if (total.signum() < 0) {
            log.debug("Attempt {} raw score {} floored to 0", attempt.getId(), total);
            total = BigDecimal.ZERO;
        }
        attempt.setScore(total);
        attempt.setMaxScore(test.getTotalMarks());
        attempt.setPassed(test.getPassingMarks() == null ? null : total.compareTo(test.getPassingMarks()) >= 0);
        attempt.setStatus(finalStatus);
        attempt.setSubmittedAt(submittedAt);
        attemptRepository.save(attempt);
        log.debug("Attempt {} graded: score={}/{}, passed={}, status={}",
                attempt.getId(), total, test.getTotalMarks(), attempt.getPassed(), finalStatus);
    }

    /** Result of grading a single answer: correctness (null = unanswered) and marks awarded. */
    record GradedAnswer(Boolean isCorrect, BigDecimal awarded) {}

    /**
     * Pure grading of one objective answer (all-or-nothing): unanswered -> 0 and null correctness;
     * an exact match of selected vs. correct options earns {@code marks}; otherwise the answer is
     * wrong, losing {@code negative} marks only when negative marking is enabled (else 0).
     */
    static GradedAnswer gradeAnswer(Set<Long> selected, Set<Long> correct,
                                    BigDecimal marks, BigDecimal negative, boolean negativeMarking) {
        if (selected == null || selected.isEmpty()) {
            return new GradedAnswer(null, BigDecimal.ZERO);
        }
        if (selected.equals(correct)) {
            return new GradedAnswer(true, marks);
        }
        return new GradedAnswer(false, negativeMarking ? negative.negate() : BigDecimal.ZERO);
    }

    // --- helpers -----------------------------------------------------------

    private void upsertAnswer(TestAttempt attempt, Test test, SubmitAnswerRequestDto dto) {
        log.debug("upsertAnswer: attempt={}, question={}, options={}", attempt.getId(), dto.questionId(), dto.selectedOptionIds());
        // The question must belong to this test.
        TestQuestion tq = testQuestionRepository.findByTestIdAndQuestionId(test.getId(), dto.questionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question " + dto.questionId() + " is not part of this test"));
        // Validate that any selected options actually belong to the question.
        String csv = null;
        if (dto.selectedOptionIds() != null && !dto.selectedOptionIds().isEmpty()) {
            Set<Long> validOptionIds = optionRepository
                    .findByQuestionIdOrderByOptionOrder(dto.questionId()).stream()
                    .map(Option::getId).collect(Collectors.toSet());
            for (Long optId : dto.selectedOptionIds()) {
                if (!validOptionIds.contains(optId)) {
                    throw new IllegalArgumentException(
                            "Option " + optId + " does not belong to question " + dto.questionId());
                }
            }
            csv = dto.selectedOptionIds().stream().distinct().map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        TestAttemptAnswer answer = answerRepository
                .findByAttemptIdAndQuestionId(attempt.getId(), dto.questionId())
                .orElseGet(() -> TestAttemptAnswer.builder()
                        .attempt(attempt)
                        .questionId(dto.questionId())
                        .build());
        answer.setSelectedOptionIds(csv);
        answer.setAnsweredAt(Instant.now());
        answerRepository.save(answer);
    }

    private TestAttemptResponseDto buildAttemptResponse(TestAttempt attempt, Test test) {
        return buildAttemptResponse(attempt, test, false);
    }

    /**
     * Builds the attempt view. {@code alwaysReveal} forces the grading fields (correctness, marks
     * awarded, correct option ids) on for a graded attempt regardless of the test's {@code showAnswers}
     * flag — used for the staff drill-down, which must always show the correct answers.
     */
    private TestAttemptResponseDto buildAttemptResponse(TestAttempt attempt, Test test, boolean alwaysReveal) {
        boolean graded = attempt.getStatus() != AttemptStatus.IN_PROGRESS;
        boolean reveal = graded && (alwaysReveal || test.isShowAnswers());

        Map<Long, TestAttemptAnswer> answerByQuestion = answerRepository.findByAttemptId(attempt.getId()).stream()
                .collect(Collectors.toMap(TestAttemptAnswer::getQuestionId, a -> a, (a, b) -> a));

        List<AttemptQuestionResponseDto> questions = new ArrayList<>();
        for (TestQuestion tq : testQuestionRepository.findByTestIdOrderBySortOrderAsc(test.getId())) {
            Question q = tq.getQuestion();
            TestAttemptAnswer answer = answerByQuestion.get(q.getId());
            List<AttemptQuestionResponseDto.OptionView> opts = q.getOptions() == null ? List.of()
                    : q.getOptions().stream()
                        .map(o -> new AttemptQuestionResponseDto.OptionView(
                                o.getId(), o.getText(), o.getOptionImagePath(), o.getOptionOrder()))
                        .toList();
            List<Long> selected = answer == null ? List.of()
                    : new ArrayList<>(parseOptionIds(answer.getSelectedOptionIds()));
            List<Long> correctIds = reveal
                    ? optionRepository.findCorrectOptionsByQuestionId(q.getId()).stream().map(Option::getId).toList()
                    : null;
            questions.add(new AttemptQuestionResponseDto(
                    q.getId(), q.getText(), q.getQuestionImagePath(), q.getQuestionType(),
                    TestService.effectiveMarks(tq), tq.getSortOrder(), opts, selected,
                    reveal && answer != null ? answer.getIsCorrect() : null,
                    reveal && answer != null ? answer.getMarksAwarded() : null,
                    correctIds));
        }

        return new TestAttemptResponseDto(
                attempt.getId(), test.getId(), test.getTitle(), attempt.getStatus(),
                attempt.getAttemptNumber(), attempt.getStartedAt(), attempt.getSubmittedAt(),
                attempt.getExpiresAt(),
                graded ? attempt.getScore() : null,
                graded ? attempt.getMaxScore() : null,
                graded ? attempt.getPassed() : null,
                questions);
    }

    /**
     * Loads a test for a student, scoped to their institute (a test in another institute reads as
     * not-found, never leaking cross-tenant existence).
     */
    private Test loadStudentTest(Long testId, Long instituteId) {
        return testRepository.findByIdAndInstituteId(testId, instituteId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));
    }

    private TestAttempt loadOwnAttempt(Long attemptId, Long studentUserId) {
        return attemptRepository.findByIdAndStudentUserId(attemptId, studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found with ID: " + attemptId));
    }

    private static Set<Long> parseOptionIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ids.add(Long.valueOf(trimmed));
            }
        }
        return ids;
    }

    private static Instant earlierOf(Instant a, Instant b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }
}
