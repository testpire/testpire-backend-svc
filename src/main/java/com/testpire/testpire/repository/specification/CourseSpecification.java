package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Course;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseSpecification {

    public static Specification<Course> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("instituteId"), instituteId);
        };
    }

    public static Specification<Course> hasNameContaining(String name) {
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

    public static Specification<Course> hasCodeContaining(String code) {
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

    public static Specification<Course> hasDescriptionContaining(String description) {
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

    public static Specification<Course> hasTextContaining(String searchText) {
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

    public static Specification<Course> hasDurationRange(Integer minDuration, Integer maxDuration) {
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

    public static Specification<Course> hasLevel(String level) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(level)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("level")),
                "%" + level.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Course> hasPrerequisitesContaining(String prerequisites) {
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

    public static Specification<Course> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Course> isNotDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Course> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Course> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Course> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(createdBy)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Course> hasSubjects() {
        return (root, query, criteriaBuilder) -> {
            Join<Course, Object> subjectsJoin = root.join("subjects", JoinType.INNER);
            return criteriaBuilder.isNotNull(subjectsJoin);
        };
    }

    public static Specification<Course> hasMinimumSubjects(Integer minSubjects) {
        return (root, query, criteriaBuilder) -> {
            if (minSubjects == null || minSubjects <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Course> subRoot = subquery.from(Course.class);
            Join<Course, Object> subjectsJoin = subRoot.join("subjects", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(subjectsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.greaterThanOrEqualTo(subquery, minSubjects.longValue());
        };
    }

    public static Specification<Course> hasMaximumSubjects(Integer maxSubjects) {
        return (root, query, criteriaBuilder) -> {
            if (maxSubjects == null || maxSubjects <= 0) {
                return criteriaBuilder.conjunction();
            }
            
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Course> subRoot = subquery.from(Course.class);
            Join<Course, Object> subjectsJoin = subRoot.join("subjects", JoinType.INNER);
            
            subquery.select(criteriaBuilder.count(subjectsJoin))
                   .where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
            
            return criteriaBuilder.lessThanOrEqualTo(subquery, maxSubjects.longValue());
        };
    }
}
