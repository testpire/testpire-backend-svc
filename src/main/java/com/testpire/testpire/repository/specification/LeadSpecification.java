package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.Lead;
import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeadSpecification {

    public static Specification<Lead> hasInstituteId(Long instituteId) {
        return (root, query, cb) ->
            instituteId == null ? null : cb.equal(root.get("instituteId"), instituteId);
    }

    public static Specification<Lead> hasStatus(LeadStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Lead> hasSource(LeadSource source) {
        return (root, query, cb) ->
            source == null ? null : cb.equal(root.get("source"), source);
    }

    public static Specification<Lead> hasInterestedCourseId(Long courseId) {
        return (root, query, cb) ->
            courseId == null ? null : cb.equal(root.get("interestedCourseId"), courseId);
    }

    public static Specification<Lead> hasAssignedTo(String assignedTo) {
        return (root, query, cb) -> {
            if (assignedTo == null || assignedTo.trim().isEmpty()) return null;
            return cb.equal(cb.lower(root.get("assignedTo")), assignedTo.toLowerCase());
        };
    }

    public static Specification<Lead> hasFirstNameContaining(String firstName) {
        return (root, query, cb) -> {
            if (firstName == null || firstName.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%");
        };
    }

    public static Specification<Lead> hasLastNameContaining(String lastName) {
        return (root, query, cb) -> {
            if (lastName == null || lastName.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }

    public static Specification<Lead> hasEmailContaining(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<Lead> hasPhoneContaining(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("phone")), "%" + phone.toLowerCase() + "%");
        };
    }

    /** true = only converted leads (convertedUserId set); false = only un-converted; null = no filter. */
    public static Specification<Lead> isConverted(Boolean converted) {
        return (root, query, cb) -> {
            if (converted == null) return null;
            return converted
                ? cb.isNotNull(root.get("convertedUserId"))
                : cb.isNull(root.get("convertedUserId"));
        };
    }

    public static Specification<Lead> followUpFrom(LocalDate from) {
        return (root, query, cb) ->
            from == null ? null : cb.greaterThanOrEqualTo(root.get("nextFollowUpDate"), from);
    }

    public static Specification<Lead> followUpTo(LocalDate to) {
        return (root, query, cb) ->
            to == null ? null : cb.lessThanOrEqualTo(root.get("nextFollowUpDate"), to);
    }

    public static Specification<Lead> createdAfter(LocalDateTime createdAfter) {
        return (root, query, cb) ->
            createdAfter == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
    }

    public static Specification<Lead> createdBefore(LocalDateTime createdBefore) {
        return (root, query, cb) ->
            createdBefore == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
    }

    public static Specification<Lead> createdBy(String createdBy) {
        return (root, query, cb) -> {
            if (createdBy == null || createdBy.trim().isEmpty()) return null;
            return cb.equal(root.get("createdBy"), createdBy);
        };
    }

    public static Specification<Lead> hasSearchText(String searchText) {
        return (root, query, cb) -> {
            if (searchText == null || searchText.trim().isEmpty()) return null;
            String like = "%" + searchText.toLowerCase() + "%";
            Predicate firstName = cb.like(cb.lower(root.get("firstName")), like);
            Predicate lastName = cb.like(cb.lower(root.get("lastName")), like);
            Predicate email = cb.like(cb.lower(root.get("email")), like);
            Predicate phone = cb.like(cb.lower(root.get("phone")), like);
            return cb.or(firstName, lastName, email, phone);
        };
    }
}
