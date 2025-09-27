package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Question;
import com.testpire.testpire.enums.DifficultyLevel;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionSpecification {

    public static Specification<Question> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("instituteId"), instituteId);
        };
    }

    public static Specification<Question> hasTopicId(Long topicId) {
        return (root, query, criteriaBuilder) -> {
            if (topicId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Question, Object> topicJoin = root.join("topic", JoinType.INNER);
            return criteriaBuilder.equal(topicJoin.get("id"), topicId);
        };
    }

    public static Specification<Question> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Question, Object> topicJoin = root.join("topic", JoinType.INNER);
            Join<Object, Object> chapterJoin = topicJoin.join("chapter", JoinType.INNER);
            Join<Object, Object> subjectJoin = chapterJoin.join("subject", JoinType.INNER);
            return criteriaBuilder.equal(subjectJoin.get("id"), subjectId);
        };
    }

    public static Specification<Question> hasChapterId(Long chapterId) {
        return (root, query, criteriaBuilder) -> {
            if (chapterId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Question, Object> topicJoin = root.join("topic", JoinType.INNER);
            Join<Object, Object> chapterJoin = topicJoin.join("chapter", JoinType.INNER);
            return criteriaBuilder.equal(chapterJoin.get("id"), chapterId);
        };
    }

    public static Specification<Question> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Question, Object> topicJoin = root.join("topic", JoinType.INNER);
            Join<Object, Object> chapterJoin = topicJoin.join("chapter", JoinType.INNER);
            Join<Object, Object> subjectJoin = chapterJoin.join("subject", JoinType.INNER);
            Join<Object, Object> courseJoin = subjectJoin.join("course", JoinType.INNER);
            return criteriaBuilder.equal(courseJoin.get("id"), courseId);
        };
    }

    public static Specification<Question> hasDifficultyLevel(DifficultyLevel difficultyLevel) {
        return (root, query, criteriaBuilder) -> {
            if (difficultyLevel == null || difficultyLevel== DifficultyLevel.ALL) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("difficultyLevel"), difficultyLevel);
        };
    }

    public static Specification<Question> hasQuestionType(String questionType) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(questionType)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("questionType")),
                "%" + questionType.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Question> hasTextContaining(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchText)) {
                return criteriaBuilder.conjunction();
            }
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("text")),
                    searchPattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("questionType")),
                    searchPattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("explanation")),
                    searchPattern
                )
            );
        };
    }

    public static Specification<Question> hasMarksRange(Integer minMarks, Integer maxMarks) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minMarks != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("marks"), minMarks));
            }
            
            if (maxMarks != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("marks"), maxMarks));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Question> hasNegativeMarksRange(Integer minNegativeMarks, Integer maxNegativeMarks) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minNegativeMarks != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("negativeMarks"), minNegativeMarks));
            }
            
            if (maxNegativeMarks != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("negativeMarks"), maxNegativeMarks));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Question> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Question> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Question> hasQuestionImage() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNotNull(root.get("questionImagePath"));
    }

    public static Specification<Question> hasExplanation() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.isNotNull(root.get("explanation")),
                criteriaBuilder.notEqual(root.get("explanation"), "")
            );
    }

    public static Specification<Question> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Question> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Question> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Question> hasCorrectOption() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNotNull(root.get("correctOptionId"));
    }

    public static Specification<Question> hasOptions() {
        return (root, query, criteriaBuilder) -> {
            Join<Question, Object> optionsJoin = root.join("options", JoinType.INNER);
            return criteriaBuilder.isNotNull(optionsJoin);
        };
    }

    public static Specification<Question> hasMinimumOptions(Integer minOptions) {
        return (root, query, criteriaBuilder) -> {
            if (minOptions == null || minOptions <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Question> subRoot = subquery.from(Question.class);
            Join<Question, Object> optionsJoin = subRoot.join("options", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(optionsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.greaterThanOrEqualTo(subquery, minOptions.longValue());
        };
    }

    public static Specification<Question> hasMaximumOptions(Integer maxOptions) {
        return (root, query, criteriaBuilder) -> {
            if (maxOptions == null || maxOptions <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Question> subRoot = subquery.from(Question.class);
            Join<Question, Object> optionsJoin = subRoot.join("options", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(optionsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.lessThanOrEqualTo(subquery, maxOptions.longValue());
        };
    }
}
