package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Option;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OptionSpecification {

    public static Specification<Option> hasQuestionId(Long questionId) {
        return (root, query, criteriaBuilder) -> {
            if (questionId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            return criteriaBuilder.equal(questionJoin.get("id"), questionId);
        };
    }

    public static Specification<Option> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            return criteriaBuilder.equal(questionJoin.get("instituteId"), instituteId);
        };
    }

    public static Specification<Option> hasTopicId(Long topicId) {
        return (root, query, criteriaBuilder) -> {
            if (topicId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            Join<Object, Object> topicJoin = questionJoin.join("topic", JoinType.INNER);
            return criteriaBuilder.equal(topicJoin.get("id"), topicId);
        };
    }

    public static Specification<Option> hasChapterId(Long chapterId) {
        return (root, query, criteriaBuilder) -> {
            if (chapterId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            Join<Object, Object> topicJoin = questionJoin.join("topic", JoinType.INNER);
            Join<Object, Object> chapterJoin = topicJoin.join("chapter", JoinType.INNER);
            return criteriaBuilder.equal(chapterJoin.get("id"), chapterId);
        };
    }

    public static Specification<Option> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            Join<Object, Object> topicJoin = questionJoin.join("topic", JoinType.INNER);
            Join<Object, Object> chapterJoin = topicJoin.join("chapter", JoinType.INNER);
            Join<Object, Object> subjectJoin = chapterJoin.join("subject", JoinType.INNER);
            return criteriaBuilder.equal(subjectJoin.get("id"), subjectId);
        };
    }

    public static Specification<Option> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            Join<Object, Object> topicJoin = questionJoin.join("topic", JoinType.INNER);
            Join<Object, Object> chapterJoin = topicJoin.join("chapter", JoinType.INNER);
            Join<Object, Object> subjectJoin = chapterJoin.join("subject", JoinType.INNER);
            Join<Object, Object> courseJoin = subjectJoin.join("course", JoinType.INNER);
            return criteriaBuilder.equal(courseJoin.get("id"), courseId);
        };
    }

    public static Specification<Option> hasTextContaining(String text) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(text)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("text")),
                "%" + text.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Option> hasOptionImage() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNotNull(root.get("optionImagePath"));
    }

    public static Specification<Option> hasOrderRange(Integer minOrder, Integer maxOrder) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minOrder != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("optionOrder"), minOrder));
            }
            
            if (maxOrder != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("optionOrder"), maxOrder));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Option> isCorrect(Boolean isCorrect) {
        return (root, query, criteriaBuilder) -> {
            if (isCorrect == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isCorrect"), isCorrect);
        };
    }

    public static Specification<Option> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Option> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Option> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Option> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Option> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Option> hasQuestionType(String questionType) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(questionType)) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            return criteriaBuilder.like(
                criteriaBuilder.lower(questionJoin.get("questionType")),
                "%" + questionType.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Option> hasDifficultyLevel(String difficultyLevel) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(difficultyLevel)) {
                return criteriaBuilder.conjunction();
            }
            Join<Option, Object> questionJoin = root.join("question", JoinType.INNER);
            return criteriaBuilder.like(
                criteriaBuilder.lower(questionJoin.get("difficultyLevel")),
                "%" + difficultyLevel.toLowerCase() + "%"
            );
        };
    }
}
