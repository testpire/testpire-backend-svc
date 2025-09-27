package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Topic;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TopicSpecification {

    public static Specification<Topic> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("instituteId"), instituteId);
        };
    }

    public static Specification<Topic> hasChapterId(Long chapterId) {
        return (root, query, criteriaBuilder) -> {
            if (chapterId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Topic, Object> chapterJoin = root.join("chapter", JoinType.INNER);
            return criteriaBuilder.equal(chapterJoin.get("id"), chapterId);
        };
    }

    public static Specification<Topic> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Topic, Object> chapterJoin = root.join("chapter", JoinType.INNER);
            Join<Object, Object> subjectJoin = chapterJoin.join("subject", JoinType.INNER);
            return criteriaBuilder.equal(subjectJoin.get("id"), subjectId);
        };
    }

    public static Specification<Topic> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Topic, Object> chapterJoin = root.join("chapter", JoinType.INNER);
            Join<Object, Object> subjectJoin = chapterJoin.join("subject", JoinType.INNER);
            Join<Object, Object> courseJoin = subjectJoin.join("course", JoinType.INNER);
            return criteriaBuilder.equal(courseJoin.get("id"), courseId);
        };
    }

    public static Specification<Topic> hasNameContaining(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Topic> hasCodeContaining(String code) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(code)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("code")),
                "%" + code.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Topic> hasDescriptionContaining(String description) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(description)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")),
                "%" + description.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Topic> hasTextContaining(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchText)) {
                return criteriaBuilder.conjunction();
            }
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    searchPattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")),
                    searchPattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    searchPattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")),
                    searchPattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("learningOutcomes")),
                    searchPattern
                )
            );
        };
    }

    public static Specification<Topic> hasOrderIndexRange(Integer minOrderIndex, Integer maxOrderIndex) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minOrderIndex != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderIndex"), minOrderIndex));
            }
            
            if (maxOrderIndex != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderIndex"), maxOrderIndex));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Topic> hasDurationRange(Integer minDuration, Integer maxDuration) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minDuration != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("duration"), minDuration));
            }
            
            if (maxDuration != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("duration"), maxDuration));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Topic> hasContentContaining(String content) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(content)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("content")),
                "%" + content.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Topic> hasLearningOutcomesContaining(String learningOutcomes) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(learningOutcomes)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("learningOutcomes")),
                "%" + learningOutcomes.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Topic> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Topic> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Topic> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Topic> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Topic> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Topic> hasQuestions() {
        return (root, query, criteriaBuilder) -> {
            Join<Topic, Object> questionsJoin = root.join("questions", JoinType.INNER);
            return criteriaBuilder.isNotNull(questionsJoin);
        };
    }

    public static Specification<Topic> hasMinimumQuestions(Integer minQuestions) {
        return (root, query, criteriaBuilder) -> {
            if (minQuestions == null || minQuestions <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Topic> subRoot = subquery.from(Topic.class);
            Join<Topic, Object> questionsJoin = subRoot.join("questions", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(questionsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.greaterThanOrEqualTo(subquery, minQuestions.longValue());
        };
    }

    public static Specification<Topic> hasMaximumQuestions(Integer maxQuestions) {
        return (root, query, criteriaBuilder) -> {
            if (maxQuestions == null || maxQuestions <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Topic> subRoot = subquery.from(Topic.class);
            Join<Topic, Object> questionsJoin = subRoot.join("questions", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(questionsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.lessThanOrEqualTo(subquery, maxQuestions.longValue());
        };
    }
}
