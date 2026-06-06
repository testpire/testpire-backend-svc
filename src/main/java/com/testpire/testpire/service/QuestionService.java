package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateOptionRequestDto;
import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.request.QuestionSearchRequestDto;
import com.testpire.testpire.dto.request.UpdateQuestionRequestDto;
import com.testpire.testpire.dto.response.QuestionListResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.entity.Option;
import com.testpire.testpire.entity.Question;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.enums.DifficultyLevel;
import com.testpire.testpire.repository.InstituteRepository;
import com.testpire.testpire.repository.OptionRepository;
import com.testpire.testpire.repository.QuestionRepository;
import com.testpire.testpire.repository.TopicRepository;
import com.testpire.testpire.repository.specification.QuestionSpecification;
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
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final TopicRepository topicRepository;
    private final InstituteRepository instituteRepository;
    private final QuestionImageService questionImageService;

    @Transactional
    public QuestionResponseDto createQuestion(CreateQuestionRequestDto request) {
        // When an external id is supplied (bulk CSV upload), re-uploading the same id updates the
        // existing question in place rather than creating a duplicate — keeping the upload idempotent.
        if (request.externalId() != null && !request.externalId().isBlank()) {
            Optional<Question> existing = questionRepository
                    .findByInstituteIdAndExternalIdAndActiveTrueAndDeletedFalse(
                            request.instituteId(), request.externalId());
            if (existing.isPresent()) {
                return updateQuestionFromRequest(existing.get(), request);
            }
        }

        log.info("Creating question for topic: {} in institute: {}", request.topicId(), request.instituteId());

        Topic topic = resolveTopic(request);

        // Create question
        Question question = Question.builder()
                .text(request.text())
                .externalId(request.externalId())
                .questionImagePath(request.questionImagePath())
                .difficultyLevel(request.difficultyLevel())
                .topic(topic)
                .instituteId(request.instituteId())
                .questionType(request.questionType())
                .marks(request.marks())
                .negativeMarks(request.negativeMarks())
                .explanation(request.explanation())
                .createdBy(getCurrentUsername())
                .build();

        question = questionRepository.save(question);

        // No external id supplied (normal create): default it to "<instituteCode>_<generated id>" so
        // every question still carries a stable, institute-scoped external id. rebuildOptions persists it.
        if (question.getExternalId() == null || question.getExternalId().isBlank()) {
            question.setExternalId(resolveInstituteCode(request.instituteId()) + "_" + question.getId());
        }

        rebuildOptions(question, request.options());

        log.info("Successfully created question with ID: {}", question.getId());
        return convertToResponseDto(question);
    }

    /** Institute code for the default external-id prefix; falls back to the numeric id if not found. */
    private String resolveInstituteCode(Long instituteId) {
        return instituteRepository.findById(instituteId)
                .map(Institute::getCode)
                .filter(code -> code != null && !code.isBlank())
                .orElse(String.valueOf(instituteId));
    }

    /**
     * Updates an existing question (matched by external id) in place from a create request: scalar
     * fields are overwritten and options are fully replaced. Used by the idempotent bulk-upload path.
     */
    private QuestionResponseDto updateQuestionFromRequest(Question question, CreateQuestionRequestDto request) {
        log.info("Updating existing question {} (externalId: {}) from bulk upload",
                question.getId(), request.externalId());

        Topic topic = resolveTopic(request);

        question.setText(request.text());
        question.setQuestionImagePath(request.questionImagePath());
        question.setDifficultyLevel(request.difficultyLevel());
        question.setTopic(topic);
        question.setQuestionType(request.questionType());
        question.setMarks(request.marks());
        question.setNegativeMarks(request.negativeMarks());
        question.setExplanation(request.explanation());
        question.setUpdatedBy(getCurrentUsername());
        question = questionRepository.save(question);

        rebuildOptions(question, request.options());

        log.info("Successfully updated question with ID: {}", question.getId());
        return convertToResponseDto(question);
    }

    private Topic resolveTopic(CreateQuestionRequestDto request) {
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + request.topicId()));
        if (!topic.getInstituteId().equals(request.instituteId())) {
            throw new IllegalArgumentException("Topic does not belong to the specified institute");
        }
        return topic;
    }

    /**
     * Soft-deletes any existing options for the question, persists the supplied options, and updates
     * {@code correctOptionId}. Shared by the create and idempotent-update paths.
     */
    private void rebuildOptions(Question question, List<CreateOptionRequestDto> optionRequests) {
        List<Option> existingOptions =
                optionRepository.findByQuestionIdAndActiveTrueAndDeletedFalseOrderByOptionOrder(question.getId());
        if (!existingOptions.isEmpty()) {
            existingOptions.forEach(option -> {
                option.setDeleted(true);
                option.setActive(false);
            });
            optionRepository.saveAll(existingOptions);
        }

        List<Option> options = optionRequests.stream()
                .map(optionRequest -> Option.builder()
                        .text(optionRequest.text())
                        .optionImagePath(optionRequest.optionImagePath())
                        .question(question)
                        .isCorrect(optionRequest.isCorrect())
                        .createdBy(getCurrentUsername())
                        .build())
                .toList();
        options = optionRepository.saveAll(options);

        Option correctOption = options.stream()
                .filter(Option::isCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("At least one option must be marked as correct"));

        question.setCorrectOptionId(correctOption.getId());
        questionRepository.save(question);
    }

    private String getCurrentUsername() {
        String username = RequestUtils.getCurrentUsername();
        return username != null ? username : "system";
    }

    private Long getCurrentUserInstituteId() {
        return RequestUtils.getCurrentUserInstituteId();
    }

    /**
     * Loads a question by id, scoped to the caller's institute. Non-SUPER_ADMIN callers are
     * restricted to their JWT institute; SUPER_ADMIN (null instituteId) uses an unscoped lookup.
     */
    private Question findQuestionScoped(Long id) {
        Long instituteId = getCurrentUserInstituteId();
        return (instituteId != null
                ? questionRepository.findByIdAndInstituteIdAndActiveTrueAndDeletedFalse(id, instituteId)
                : questionRepository.findByIdAndActiveTrueAndDeletedFalse(id))
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + id));
    }

    private QuestionResponseDto convertToResponseDto(Question question) {
        List<Option> options = optionRepository.findByQuestionIdAndActiveTrueAndDeletedFalseOrderByOptionOrder(question.getId());
        
        return QuestionResponseDto.builder()
                .id(question.getId())
                .externalId(question.getExternalId())
                .text(question.getText())
                .questionImagePath(questionImageService.toPublicUrl(question.getQuestionImagePath()))
                .difficultyLevel(question.getDifficultyLevel())
                .topicId(question.getTopic().getId())
                .topicName(question.getTopic().getName())
                .correctOptionId(question.getCorrectOptionId())
                .instituteId(question.getInstituteId())
                .questionType(question.getQuestionType())
                .marks(question.getMarks())
                .negativeMarks(question.getNegativeMarks())
                .explanation(question.getExplanation())
                .options(options.stream()
                        .map(this::convertOptionToResponseDto)
                        .toList())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .createdBy(question.getCreatedBy())
                .updatedBy(question.getUpdatedBy())
                .active(question.isActive())
                .build();
    }

    private com.testpire.testpire.dto.response.OptionResponseDto convertOptionToResponseDto(Option option) {
        return com.testpire.testpire.dto.response.OptionResponseDto.builder()
                .id(option.getId())
                .text(option.getText())
                .optionImagePath(questionImageService.toPublicUrl(option.getOptionImagePath()))
                .questionId(option.getQuestion().getId())
                .optionOrder(option.getOptionOrder())
                .isCorrect(option.isCorrect())
                .createdAt(option.getCreatedAt())
                .updatedAt(option.getUpdatedAt())
                .createdBy(option.getCreatedBy())
                .updatedBy(option.getUpdatedBy())
                .active(option.isActive())
                .build();
    }

    @Transactional
    public QuestionResponseDto updateQuestion(Long id, UpdateQuestionRequestDto request) {
        log.info("Updating question with ID: {}", id);

        Question question = findQuestionScoped(id);

        // Update question fields
        question.setText(request.text());
        question.setQuestionImagePath(request.questionImagePath());
        question.setDifficultyLevel(request.difficultyLevel());
        question.setQuestionType(request.questionType());
        question.setMarks(request.marks());
        question.setNegativeMarks(request.negativeMarks());
        question.setExplanation(request.explanation());
        question.setUpdatedBy(getCurrentUsername());

        if (request.active() != null) {
            question.setActive(request.active());
        }

        // Update options
        List<Option> existingOptions = optionRepository.findByQuestionIdAndActiveTrueAndDeletedFalseOrderByOptionOrder(id);
        
        // Remove existing options
        existingOptions.forEach(option -> {
            option.setDeleted(true);
            option.setActive(false);
        });
        optionRepository.saveAll(existingOptions);

        // Create new options; question is captured here before the reassignment below.
        final Question savedQuestion = question;
        List<Option> newOptions = request.options().stream()
                .map(optionRequest -> Option.builder()
                        .text(optionRequest.text())
                        .optionImagePath(optionRequest.optionImagePath())
                        .question(savedQuestion)
                        .optionOrder(optionRequest.optionOrder())
                        .isCorrect(optionRequest.isCorrect())
                        .createdBy(getCurrentUsername())
                        .build())
                .toList();

        newOptions = optionRepository.saveAll(newOptions);

        // Set correct option ID
        Option correctOption = newOptions.stream()
                .filter(Option::isCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("At least one option must be marked as correct"));

        question.setCorrectOptionId(correctOption.getId());
        question = questionRepository.save(question);

        log.info("Successfully updated question with ID: {}", id);
        return convertToResponseDto(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        log.info("Deleting question with ID: {}", id);

        Question question = findQuestionScoped(id);

        // Soft delete question (options will be cascade deleted)
        question.setDeleted(true);
        question.setActive(false);
        question.setUpdatedBy(getCurrentUsername());
        questionRepository.save(question);

        log.info("Successfully deleted question with ID: {}", id);
    }

    public QuestionResponseDto getQuestionById(Long id) {
        log.info("Getting question with ID: {}", id);
        return convertToResponseDto(findQuestionScoped(id));
    }

    public QuestionListResponseDto getQuestionsByTopic(Long topicId, Long instituteId) {
        log.info("Getting questions for topic: {} in institute: {}", topicId, instituteId);

        List<Question> questions = (instituteId != null)
                ? questionRepository.findByTopicIdAndInstituteIdAndActiveTrueAndDeletedFalse(topicId, instituteId)
                : questionRepository.findByTopicIdAndActiveTrueAndDeletedFalse(topicId);
        
        List<QuestionResponseDto> questionDtos = questions.stream()
                .map(this::convertToResponseDto)
                .toList();

        return QuestionListResponseDto.builder()
                .questions(questionDtos)
                .totalCount((long) questionDtos.size())
                .build();
    }

    public QuestionListResponseDto getQuestionsByInstitute(Long instituteId) {
        log.info("Getting questions for institute: {}", instituteId);

        List<Question> questions = questionRepository.findByInstituteIdAndActiveTrueAndDeletedFalse(instituteId);
        
        List<QuestionResponseDto> questionDtos = questions.stream()
                .map(this::convertToResponseDto)
                .toList();

        return QuestionListResponseDto.builder()
                .questions(questionDtos)
                .totalCount((long) questionDtos.size())
                .build();
    }


    @Transactional(readOnly = true)
    public QuestionListResponseDto searchQuestionsWithSpecification(QuestionSearchRequestDto request) {
        log.info("Searching questions with specification: {}", request);

        // Build specification
        Specification<Question> spec = buildSpecification(request);

        // Create pageable
        Pageable pageable = createPageable(request);

        // Execute search
        Page<Question> questionPage = questionRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<QuestionResponseDto> questionDtos = questionPage.getContent().stream()
                .map(this::convertToResponseDto)
                .toList();

        return QuestionListResponseDto.builder()
                .questions(questionDtos)
                .totalCount(questionPage.getTotalElements())
                .build();
    }

    private Specification<Question> buildSpecification(QuestionSearchRequestDto request) {
        return Specification.where(QuestionSpecification.hasInstituteId(request.getInstituteId()))
                .and(QuestionSpecification.hasCourseId(request.getCourseId()))
                .and(QuestionSpecification.hasSubjectId(request.getSubjectId()))
                .and(QuestionSpecification.hasChapterId(request.getChapterId()))
                .and(QuestionSpecification.hasTopicId(request.getTopicId()))
                .and(QuestionSpecification.hasTextContaining(request.getSearchText()))
                .and(QuestionSpecification.hasDifficultyLevel(request.getDifficultyLevel()))
                .and(QuestionSpecification.hasQuestionType(request.getQuestionType()))
                .and(QuestionSpecification.hasMarksRange(request.getMinMarks(), request.getMaxMarks()))
                .and(QuestionSpecification.hasNegativeMarksRange(request.getMinNegativeMarks(), request.getMaxNegativeMarks()))
                .and(QuestionSpecification.isActive(request.getActive()))
                .and(QuestionSpecification.isNotDeleted())
                .and(request.getHasQuestionImage() != null && request.getHasQuestionImage() ? 
                     QuestionSpecification.hasQuestionImage() : (root, query, cb) -> cb.conjunction())
                .and(request.getHasExplanation() != null && request.getHasExplanation() ? 
                     QuestionSpecification.hasExplanation() : (root, query, cb) -> cb.conjunction())
                .and(request.getHasCorrectOption() != null && request.getHasCorrectOption() ? 
                     QuestionSpecification.hasCorrectOption() : (root, query, cb) -> cb.conjunction())
                .and(request.getHasOptions() != null && request.getHasOptions() ? 
                     QuestionSpecification.hasOptions() : (root, query, cb) -> cb.conjunction())
                .and(QuestionSpecification.hasMinimumOptions(request.getMinOptions()))
                .and(QuestionSpecification.hasMaximumOptions(request.getMaxOptions()))
                .and(QuestionSpecification.createdAfter(request.getCreatedAfter()))
                .and(QuestionSpecification.createdBefore(request.getCreatedBefore()))
                .and(QuestionSpecification.createdBy(request.getCreatedBy()));
    }

    private Pageable createPageable(QuestionSearchRequestDto request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
}