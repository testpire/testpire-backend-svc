package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Chapter;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChapterSpecification {

    public static Specification<Chapter> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("instituteId"), instituteId);
        };
    }

    public static Specification<Chapter> hasSubjectId(Long subjectId) {
        return (root, query, criteriaBuilder) -> {
            if (subjectId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Chapter, Object> subjectJoin = root.join("subject", JoinType.INNER);
            return criteriaBuilder.equal(subjectJoin.get("id"), subjectId);
        };
    }

    public static Specification<Chapter> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Chapter, Object> subjectJoin = root.join("subject", JoinType.INNER);
            Join<Object, Object> courseJoin = subjectJoin.join("course", JoinType.INNER);
            return criteriaBuilder.equal(courseJoin.get("id"), courseId);
        };
    }

    public static Specification<Chapter> hasNameContaining(String name) {
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

    public static Specification<Chapter> hasCodeContaining(String code) {
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

    public static Specification<Chapter> hasDescriptionContaining(String description) {
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

    public static Specification<Chapter> hasTextContaining(String searchText) {
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
                    criteriaBuilder.lower(root.get("objectives")),
                    searchPattern
                )
            );
        };
    }

    public static Specification<Chapter> hasOrderIndexRange(Integer minOrderIndex, Integer maxOrderIndex) {
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

    public static Specification<Chapter> hasDurationRange(Integer minDuration, Integer maxDuration) {
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

    public static Specification<Chapter> hasObjectivesContaining(String objectives) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(objectives)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("objectives")),
                "%" + objectives.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Chapter> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Chapter> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Chapter> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Chapter> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Chapter> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Chapter> hasTopics() {
        return (root, query, criteriaBuilder) -> {
            Join<Chapter, Object> topicsJoin = root.join("topics", JoinType.INNER);
            return criteriaBuilder.isNotNull(topicsJoin);
        };
    }

    public static Specification<Chapter> hasMinimumTopics(Integer minTopics) {
        return (root, query, criteriaBuilder) -> {
            if (minTopics == null || minTopics <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Chapter> subRoot = subquery.from(Chapter.class);
            Join<Chapter, Object> topicsJoin = subRoot.join("topics", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(topicsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.greaterThanOrEqualTo(subquery, minTopics.longValue());
        };
    }

    public static Specification<Chapter> hasMaximumTopics(Integer maxTopics) {
        return (root, query, criteriaBuilder) -> {
            if (maxTopics == null || maxTopics <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Chapter> subRoot = subquery.from(Chapter.class);
            Join<Chapter, Object> topicsJoin = subRoot.join("topics", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(topicsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.lessThanOrEqualTo(subquery, maxTopics.longValue());
        };
    }
}
