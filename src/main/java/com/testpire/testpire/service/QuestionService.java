package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.request.QuestionSearchRequestDto;
import com.testpire.testpire.dto.request.UpdateQuestionRequestDto;
import com.testpire.testpire.dto.response.QuestionListResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import com.testpire.testpire.entity.Option;
import com.testpire.testpire.entity.Question;
import com.testpire.testpire.entity.Topic;
import com.testpire.testpire.enums.DifficultyLevel;
import com.testpire.testpire.repository.OptionRepository;
import com.testpire.testpire.repository.QuestionRepository;
import com.testpire.testpire.repository.TopicRepository;
import com.testpire.testpire.repository.specification.QuestionSpecification;
import com.testpire.testpire.util.JwksJwtUtil;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final TopicRepository topicRepository;
    private final JwksJwtUtil jwtUtil;

    @Transactional
    public QuestionResponseDto createQuestion(CreateQuestionRequestDto request) {
        log.info("Creating question for topic: {} in institute: {}", request.topicId(), request.instituteId());

        // Validate topic exists and user has access
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found with ID: " + request.topicId()));

        if (!topic.getInstituteId().equals(request.instituteId())) {
            throw new IllegalArgumentException("Topic does not belong to the specified institute");
        }

        // Create question
        Question question = Question.builder()
                .text(request.text())
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

        // Create options
        final Question finalQuestion = question;
        List<Option> options = request.options().stream()
                .map(optionRequest -> Option.builder()
                        .text(optionRequest.text())
                        .optionImagePath(optionRequest.optionImagePath())
                        .question(finalQuestion)
                        .isCorrect(optionRequest.isCorrect())
                        .createdBy(getCurrentUsername())
                        .build())
                .collect(Collectors.toList());

        options = optionRepository.saveAll(options);

        // Set correct option ID
        Option correctOption = options.stream()
                .filter(Option::isCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("At least one option must be marked as correct"));

        question.setCorrectOptionId(correctOption.getId());
        question = questionRepository.save(question);

        log.info("Successfully created question with ID: {}", question.getId());
        return convertToResponseDto(question);
    }

    private String getCurrentUsername() {
        return "system"; // Placeholder
    }

    private Long getCurrentUserInstituteId() {
        return 1L; // Placeholder
    }

    private QuestionResponseDto convertToResponseDto(Question question) {
        List<Option> options = optionRepository.findByQuestionIdAndActiveTrueAndDeletedFalseOrderByOptionOrder(question.getId());
        
        return QuestionResponseDto.builder()
                .id(question.getId())
                .text(question.getText())
                .questionImagePath(question.getQuestionImagePath())
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
                        .collect(Collectors.toList()))
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
                .optionImagePath(option.getOptionImagePath())
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

        Question question = questionRepository.findByIdAndInstituteIdAndActiveTrueAndDeletedFalse(id, getCurrentUserInstituteId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + id));

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

        // Create new options
        final Question finalQuestionForUpdate = question;
        List<Option> newOptions = request.options().stream()
                .map(optionRequest -> Option.builder()
                        .text(optionRequest.text())
                        .optionImagePath(optionRequest.optionImagePath())
                        .question(finalQuestionForUpdate)
                        .optionOrder(optionRequest.optionOrder())
                        .isCorrect(optionRequest.isCorrect())
                        .createdBy(getCurrentUsername())
                        .build())
                .collect(Collectors.toList());

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

        Question question = questionRepository.findByIdAndInstituteIdAndActiveTrueAndDeletedFalse(id, getCurrentUserInstituteId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + id));

        // Soft delete question (options will be cascade deleted)
        question.setDeleted(true);
        question.setActive(false);
        question.setUpdatedBy(getCurrentUsername());
        questionRepository.save(question);

        log.info("Successfully deleted question with ID: {}", id);
    }

    public QuestionResponseDto getQuestionById(Long id) {
        log.info("Getting question with ID: {}", id);

        Question question = questionRepository.findByIdAndInstituteIdAndActiveTrueAndDeletedFalse(id, getCurrentUserInstituteId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + id));

        return convertToResponseDto(question);
    }

    public QuestionListResponseDto getQuestionsByTopic(Long topicId, Long instituteId) {
        log.info("Getting questions for topic: {} in institute: {}", topicId, instituteId);

        List<Question> questions = questionRepository.findByTopicIdAndInstituteIdAndActiveTrueAndDeletedFalse(topicId, instituteId);
        
        List<QuestionResponseDto> questionDtos = questions.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
                     QuestionSpecification.hasQuestionImage() : null)
                .and(request.getHasExplanation() != null && request.getHasExplanation() ? 
                     QuestionSpecification.hasExplanation() : null)
                .and(request.getHasCorrectOption() != null && request.getHasCorrectOption() ? 
                     QuestionSpecification.hasCorrectOption() : null)
                .and(request.getHasOptions() != null && request.getHasOptions() ? 
                     QuestionSpecification.hasOptions() : null)
                .and(QuestionSpecification.hasMinimumOptions(request.getMinOptions()))
                .and(QuestionSpecification.hasMaximumOptions(request.getMaxOptions()))
                .and(QuestionSpecification.createdAfter(request.getCreatedAfter()))
                .and(QuestionSpecification.createdBefore(request.getCreatedBefore()))
                .and(QuestionSpecification.createdBy(request.getCreatedBy()));
    }

    private Pageable createPageable(QuestionSearchRequestDto request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "desc";
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        
        return PageRequest.of(page, size, sort);
    }
}