package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Subject;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubjectSpecification {

    public static Specification<Subject> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("instituteId"), instituteId);
        };
    }

    public static Specification<Subject> hasCourseId(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Subject, Object> courseJoin = root.join("course", JoinType.INNER);
            return criteriaBuilder.equal(courseJoin.get("id"), courseId);
        };
    }

    public static Specification<Subject> hasNameContaining(String name) {
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

    public static Specification<Subject> hasCodeContaining(String code) {
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

    public static Specification<Subject> hasDescriptionContaining(String description) {
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

    public static Specification<Subject> hasTextContaining(String searchText) {
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
                )
            );
        };
    }

    public static Specification<Subject> hasDurationRange(Integer minDuration, Integer maxDuration) {
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

    public static Specification<Subject> hasCreditsRange(Integer minCredits, Integer maxCredits) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minCredits != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("credits"), minCredits));
            }
            
            if (maxCredits != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("credits"), maxCredits));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Subject> hasPrerequisitesContaining(String prerequisites) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(prerequisites)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("prerequisites")),
                "%" + prerequisites.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Subject> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Subject> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Subject> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Subject> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Subject> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Subject> hasChapters() {
        return (root, query, criteriaBuilder) -> {
            Join<Subject, Object> chaptersJoin = root.join("chapters", JoinType.INNER);
            return criteriaBuilder.isNotNull(chaptersJoin);
        };
    }

    public static Specification<Subject> hasMinimumChapters(Integer minChapters) {
        return (root, query, criteriaBuilder) -> {
            if (minChapters == null || minChapters <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Subject> subRoot = subquery.from(Subject.class);
            Join<Subject, Object> chaptersJoin = subRoot.join("chapters", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(chaptersJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.greaterThanOrEqualTo(subquery, minChapters.longValue());
        };
    }

    public static Specification<Subject> hasMaximumChapters(Integer maxChapters) {
        return (root, query, criteriaBuilder) -> {
            if (maxChapters == null || maxChapters <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Subject> subRoot = subquery.from(Subject.class);
            Join<Subject, Object> chaptersJoin = subRoot.join("chapters", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(chaptersJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.lessThanOrEqualTo(subquery, maxChapters.longValue());
        };
    }
}
