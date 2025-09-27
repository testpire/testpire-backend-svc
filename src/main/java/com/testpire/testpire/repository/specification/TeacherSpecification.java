package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.TeacherDetails;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TeacherSpecification {
    
    public static Specification<TeacherDetails> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) return null;
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("instituteId"), instituteId);
        };
    }
    
    public static Specification<TeacherDetails> hasFirstNameContaining(String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.trim().isEmpty()) return null;
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("firstName")), 
                "%" + firstName.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasLastNameContaining(String lastName) {
        return (root, query, criteriaBuilder) -> {
            if (lastName == null || lastName.trim().isEmpty()) return null;
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("lastName")), 
                "%" + lastName.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasUsernameContaining(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.trim().isEmpty()) return null;
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("username")), 
                "%" + username.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasEmailContaining(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty()) return null;
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("email")), 
                "%" + email.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasPhoneContaining(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (phone == null || phone.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("phone")), 
                "%" + phone.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasDepartment(String department) {
        return (root, query, criteriaBuilder) -> {
            if (department == null || department.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("department"), department);
        };
    }
    
    public static Specification<TeacherDetails> hasSubject(String subject) {
        return (root, query, criteriaBuilder) -> {
            if (subject == null || subject.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("subject"), subject);
        };
    }
    
    public static Specification<TeacherDetails> hasQualificationContaining(String qualification) {
        return (root, query, criteriaBuilder) -> {
            if (qualification == null || qualification.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("qualification")), 
                "%" + qualification.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasExperienceYearsRange(Integer minYears, Integer maxYears) {
        return (root, query, criteriaBuilder) -> {
            if (minYears == null && maxYears == null) return null;
            
            Predicate predicate = null;
            if (minYears != null) {
                predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("experienceYears"), minYears);
            }
            if (maxYears != null) {
                Predicate maxPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("experienceYears"), maxYears);
                predicate = predicate == null ? maxPredicate : criteriaBuilder.and(predicate, maxPredicate);
            }
            return predicate;
        };
    }
    
    public static Specification<TeacherDetails> hasSpecializationContaining(String specialization) {
        return (root, query, criteriaBuilder) -> {
            if (specialization == null || specialization.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("specialization")), 
                "%" + specialization.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> hasBioContaining(String bio) {
        return (root, query, criteriaBuilder) -> {
            if (bio == null || bio.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("bio")), 
                "%" + bio.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<TeacherDetails> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) return null;
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("enabled"), enabled);
        };
    }
    
    public static Specification<TeacherDetails> hasSearchText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.trim().isEmpty()) return null;
            
            Join<TeacherDetails, User> userJoin = root.join("user");
            
            Predicate firstNamePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("firstName")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            Predicate lastNamePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("lastName")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            Predicate usernamePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("username")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            Predicate departmentPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("department")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            Predicate subjectPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("subject")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            return criteriaBuilder.or(
                firstNamePredicate,
                lastNamePredicate,
                usernamePredicate,
                departmentPredicate,
                subjectPredicate
            );
        };
    }
    
    public static Specification<TeacherDetails> createdAfter(LocalDateTime createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }
    
    public static Specification<TeacherDetails> createdBefore(LocalDateTime createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }
    
    public static Specification<TeacherDetails> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (createdBy == null || createdBy.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }
    
    public static Specification<TeacherDetails> isTeacher() {
        return (root, query, criteriaBuilder) -> {
            Join<TeacherDetails, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("role"), UserRole.TEACHER);
        };
    }
}
