package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.AddTestQuestionsRequestDto;
import com.testpire.testpire.dto.request.CreateTestRequestDto;
import com.testpire.testpire.dto.request.UpdateTestRequestDto;
import com.testpire.testpire.dto.response.TestQuestionResponseDto;
import com.testpire.testpire.dto.response.TestResponseDto;
import com.testpire.testpire.entity.Question;
import com.testpire.testpire.entity.Test;
import com.testpire.testpire.entity.TestQuestion;
import com.testpire.testpire.enums.TestStatus;
import com.testpire.testpire.repository.QuestionRepository;
import com.testpire.testpire.repository.TestQuestionRepository;
import com.testpire.testpire.repository.TestRepository;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CRUD and question-curation for tests. Multi-tenancy: a non-SUPER_ADMIN caller is scoped to their JWT
 * institute; SUPER_ADMIN may target any institute (resolved via {@link RequestUtils}). Every question
 * added must belong to the same institute as the test.
 *
 * <p>Question mutations (add/remove/reorder/re-mark) are only allowed while the test is in
 * {@code DRAFT}; publishing freezes the question set so in-flight attempts stay consistent. The
 * derived {@code totalMarks} is recomputed on every question change.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public TestResponseDto createTest(CreateTestRequestDto request) {
        Long instituteId = RequestUtils.resolveInstituteId(request.instituteId());
        if (instituteId == null) {
            throw new IllegalArgumentException("Institute ID is required");
        }
        log.debug("Creating test '{}' for institute {} (duration={}m, maxAttempts={}, negativeMarking={}, window=[{}, {}])",
                request.title(), instituteId, request.durationMinutes(), request.maxAttempts(),
                request.negativeMarking(), request.availableFrom(), request.availableUntil());
        validateWindow(request.availableFrom(), request.availableUntil());

        Test test = Test.builder()
                .title(request.title())
                .description(request.description())
                .instituteId(instituteId)
                .durationMinutes(request.durationMinutes())
                .maxAttempts(request.maxAttempts() != null ? request.maxAttempts() : 1)
                .passingMarks(request.passingMarks())
                .negativeMarking(Boolean.TRUE.equals(request.negativeMarking()))
                .shuffleQuestions(Boolean.TRUE.equals(request.shuffleQuestions()))
                .showAnswers(Boolean.TRUE.equals(request.showAnswers()))
                .availableFrom(request.availableFrom())
                .availableUntil(request.availableUntil())
                .status(TestStatus.DRAFT)
                .totalMarks(BigDecimal.ZERO)
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        Test saved = testRepository.save(test);
        log.info("Test created with ID {} (institute {})", saved.getId(), instituteId);
        return TestResponseDto.detail(saved, List.of());
    }

    @Transactional
    public TestResponseDto updateTest(Long id, UpdateTestRequestDto request) {
        log.debug("Updating test {}", id);
        Test test = findScoped(id);
        log.debug("Test {} found: title='{}', status={}", id, test.getTitle(), test.getStatus());

        Optional.ofNullable(request.title()).ifPresent(test::setTitle);
        Optional.ofNullable(request.description()).ifPresent(test::setDescription);
        Optional.ofNullable(request.durationMinutes()).ifPresent(test::setDurationMinutes);
        Optional.ofNullable(request.maxAttempts()).ifPresent(test::setMaxAttempts);
        Optional.ofNullable(request.passingMarks()).ifPresent(test::setPassingMarks);
        Optional.ofNullable(request.negativeMarking()).ifPresent(test::setNegativeMarking);
        Optional.ofNullable(request.shuffleQuestions()).ifPresent(test::setShuffleQuestions);
        Optional.ofNullable(request.showAnswers()).ifPresent(test::setShowAnswers);
        Optional.ofNullable(request.availableFrom()).ifPresent(test::setAvailableFrom);
        Optional.ofNullable(request.availableUntil()).ifPresent(test::setAvailableUntil);
        validateWindow(test.getAvailableFrom(), test.getAvailableUntil());
        test.setUpdatedBy(RequestUtils.getCurrentUsername());

        Test saved = testRepository.save(test);
        log.info("Test updated with ID: {}", saved.getId());
        return toDetail(saved);
    }

    @Transactional
    public void deleteTest(Long id) {
        Test test = findScoped(id);
        testRepository.delete(test);
        log.info("Test deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public TestResponseDto getTestById(Long id) {
        return toDetail(findScoped(id));
    }

    @Transactional(readOnly = true)
    public List<TestResponseDto> listTests() {
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        log.debug("Listing tests for institute {} (null = all)", instituteId);
        List<Test> tests = instituteId != null
                ? testRepository.findByInstituteId(instituteId)
                : testRepository.findAll();
        log.debug("Found {} test(s) for institute {}", tests.size(), instituteId);
        return tests.stream()
                .map(t -> TestResponseDto.summary(t, t.getTestQuestions().size()))
                .toList();
    }

    // --- Question curation -------------------------------------------------

    @Transactional
    public TestResponseDto addQuestions(Long testId, AddTestQuestionsRequestDto request) {
        Test test = findScoped(testId);
        requireDraft(test);

        int appendOrder = test.getTestQuestions().stream()
                .mapToInt(tq -> tq.getSortOrder() == null ? 0 : tq.getSortOrder())
                .max().orElse(-1) + 1;

        for (AddTestQuestionsRequestDto.TestQuestionItem item : request.questions()) {
            Question question = questionRepository
                    .findByIdAndInstituteId(item.questionId(), test.getInstituteId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Question not found in this institute with ID: " + item.questionId()));

            Integer order = item.sortOrder() != null ? item.sortOrder() : appendOrder++;
            TestQuestion existing = testQuestionRepository
                    .findByTestIdAndQuestionId(testId, item.questionId()).orElse(null);
            if (existing != null) {
                existing.setMarks(item.marks());
                existing.setNegativeMarks(item.negativeMarks());
                existing.setSortOrder(order);
                testQuestionRepository.save(existing);
            } else {
                testQuestionRepository.save(TestQuestion.builder()
                        .test(test)
                        .question(question)
                        .marks(item.marks())
                        .negativeMarks(item.negativeMarks())
                        .sortOrder(order)
                        .addedBy(RequestUtils.getCurrentUsername())
                        .build());
            }
        }
        recomputeTotalMarks(test);
        log.info("Added/updated {} question(s) on test {}", request.questions().size(), testId);
        return toDetail(findScoped(testId));
    }

    @Transactional
    public TestResponseDto removeQuestion(Long testId, Long questionId) {
        Test test = findScoped(testId);
        requireDraft(test);
        TestQuestion tq = testQuestionRepository.findByTestIdAndQuestionId(testId, questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question " + questionId + " is not on this test"));
        testQuestionRepository.delete(tq);
        recomputeTotalMarks(test);
        log.info("Removed question {} from test {}", questionId, testId);
        return toDetail(findScoped(testId));
    }

    @Transactional
    public TestResponseDto publish(Long testId) {
        log.debug("Publish requested for test {}", testId);
        Test test = findScoped(testId);
        if (test.getStatus() == TestStatus.PUBLISHED) {
            log.debug("Test {} already published — no-op", testId);
            return toDetail(test);
        }
        List<TestQuestion> questions = testQuestionRepository.findByTestIdOrderBySortOrderAsc(testId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Cannot publish a test with no questions");
        }
        log.debug("Publishing test {}: {} questions, totalMarks before recompute={}", testId, questions.size(), test.getTotalMarks());
        recomputeTotalMarks(test);
        test.setStatus(TestStatus.PUBLISHED);
        test.setUpdatedBy(RequestUtils.getCurrentUsername());
        testRepository.save(test);
        log.info("Test {} published", testId);
        return toDetail(test);
    }

    // --- helpers -----------------------------------------------------------

    /** Effective marks for a test question: per-test override, else the question's own marks, else 0. */
    public static BigDecimal effectiveMarks(TestQuestion tq) {
        if (tq.getMarks() != null) {
            return tq.getMarks();
        }
        Integer qMarks = tq.getQuestion().getMarks();
        return qMarks != null ? BigDecimal.valueOf(qMarks) : BigDecimal.ZERO;
    }

    /** Effective negative marks: per-test override, else the question's own, else 0. */
    public static BigDecimal effectiveNegativeMarks(TestQuestion tq) {
        if (tq.getNegativeMarks() != null) {
            return tq.getNegativeMarks();
        }
        Integer qNeg = tq.getQuestion().getNegativeMarks();
        return qNeg != null ? BigDecimal.valueOf(qNeg) : BigDecimal.ZERO;
    }

    private void recomputeTotalMarks(Test test) {
        BigDecimal total = testQuestionRepository.findByTestIdOrderBySortOrderAsc(test.getId()).stream()
                .map(TestService::effectiveMarks)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        test.setTotalMarks(total);
        testRepository.save(test);
    }

    private void requireDraft(Test test) {
        if (test.getStatus() != TestStatus.DRAFT) {
            throw new IllegalStateException(
                    "Questions can only be modified while the test is in DRAFT (current: " + test.getStatus() + ")");
        }
    }

    private TestResponseDto toDetail(Test test) {
        List<TestQuestionResponseDto> questions = testQuestionRepository
                .findByTestIdOrderBySortOrderAsc(test.getId()).stream()
                .map(tq -> TestQuestionResponseDto.fromEntity(tq, effectiveMarks(tq), effectiveNegativeMarks(tq)))
                .toList();
        return TestResponseDto.detail(test, questions);
    }

    private void validateWindow(java.time.Instant from, java.time.Instant until) {
        if (from != null && until != null && until.isBefore(from)) {
            throw new IllegalArgumentException("availableUntil cannot be before availableFrom");
        }
    }

    /**
     * Loads a test scoped to the caller's institute. Non-SUPER_ADMIN users are restricted to their JWT
     * institute (a test in another institute reads as not-found). SUPER_ADMIN (null) uses the unscoped lookup.
     */
    Test findScoped(Long id) {
        Long instituteId = RequestUtils.getCurrentUserInstituteId();
        return (instituteId != null
                ? testRepository.findByIdAndInstituteId(id, instituteId)
                : testRepository.findById(id))
                .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + id));
    }
}
