package com.testpire.testpire.repository.specification;

import com.testpire.testpire.entity.StudentDetails;
import com.testpire.testpire.entity.StudentEnrollment;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class StudentSpecification {
    
    public static Specification<StudentDetails> hasInstituteId(Long instituteId) {
        return (root, query, criteriaBuilder) -> {
            if (instituteId == null) return null;
            Join<StudentDetails, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("instituteId"), instituteId);
        };
    }
    
    public static Specification<StudentDetails> hasFirstNameContaining(String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.trim().isEmpty()) return null;
            Join<StudentDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("firstName")), 
                "%" + firstName.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasLastNameContaining(String lastName) {
        return (root, query, criteriaBuilder) -> {
            if (lastName == null || lastName.trim().isEmpty()) return null;
            Join<StudentDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("lastName")), 
                "%" + lastName.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasUsernameContaining(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.trim().isEmpty()) return null;
            Join<StudentDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("username")), 
                "%" + username.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasEmailContaining(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty()) return null;
            Join<StudentDetails, User> userJoin = root.join("user");
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("email")), 
                "%" + email.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasPhoneContaining(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (phone == null || phone.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("phone")), 
                "%" + phone.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasCourse(String course) {
        return (root, query, criteriaBuilder) -> {
            if (course == null || course.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("course"), course);
        };
    }
    
    public static Specification<StudentDetails> hasCurrentClassRange(Integer minClass, Integer maxClass) {
        return (root, query, criteriaBuilder) -> {
            if (minClass == null && maxClass == null) return null;

            Predicate predicate = null;
            if (minClass != null) {
                predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("currentClass"), minClass);
            }
            if (maxClass != null) {
                Predicate maxPredicate = criteriaBuilder.lessThanOrEqualTo(root.get("currentClass"), maxClass);
                predicate = predicate == null ? maxPredicate : criteriaBuilder.and(predicate, maxPredicate);
            }
            return predicate;
        };
    }

    public static Specification<StudentDetails> hasRollNumberContaining(String rollNumber) {
        return (root, query, criteriaBuilder) -> {
            if (rollNumber == null || rollNumber.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("rollNumber")), 
                "%" + rollNumber.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasParentNameContaining(String parentName) {
        return (root, query, criteriaBuilder) -> {
            if (parentName == null || parentName.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("parentName")), 
                "%" + parentName.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasParentPhoneContaining(String parentPhone) {
        return (root, query, criteriaBuilder) -> {
            if (parentPhone == null || parentPhone.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("parentPhone")), 
                "%" + parentPhone.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasParentEmailContaining(String parentEmail) {
        return (root, query, criteriaBuilder) -> {
            if (parentEmail == null || parentEmail.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("parentEmail")), 
                "%" + parentEmail.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasAddressContaining(String address) {
        return (root, query, criteriaBuilder) -> {
            if (address == null || address.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("address")), 
                "%" + address.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasBloodGroup(String bloodGroup) {
        return (root, query, criteriaBuilder) -> {
            if (bloodGroup == null || bloodGroup.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("bloodGroup"), bloodGroup);
        };
    }
    
    public static Specification<StudentDetails> hasEmergencyContactContaining(String emergencyContact) {
        return (root, query, criteriaBuilder) -> {
            if (emergencyContact == null || emergencyContact.trim().isEmpty()) return null;
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("emergencyContact")), 
                "%" + emergencyContact.toLowerCase() + "%"
            );
        };
    }
    
    public static Specification<StudentDetails> hasSearchText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.trim().isEmpty()) return null;
            
            Join<StudentDetails, User> userJoin = root.join("user");
            
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
            
            Predicate coursePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("course")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            Predicate rollNumberPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("rollNumber")), 
                "%" + searchText.toLowerCase() + "%"
            );
            
            return criteriaBuilder.or(
                firstNamePredicate,
                lastNamePredicate,
                usernamePredicate,
                coursePredicate,
                rollNumberPredicate
            );
        };
    }
    
    public static Specification<StudentDetails> createdAfter(Instant createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) return null;
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }
    
    public static Specification<StudentDetails> createdBefore(Instant createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) return null;
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }
    
    public static Specification<StudentDetails> createdBy(String createdBy) {
        return (root, query, criteriaBuilder) -> {
            if (createdBy == null || createdBy.trim().isEmpty()) return null;
            return criteriaBuilder.equal(root.get("createdBy"), createdBy);
        };
    }
    
    /**
     * Matches students who have an enrollment in the given course. {@link StudentEnrollment} has no
     * JPA association to {@link StudentDetails}, so this correlates via a subquery on
     * {@code student_user_id} = the student's user id (mirrors {@code StudentDetailsRepository.findByBatchId}).
     */
    public static Specification<StudentDetails> hasEnrollmentInCourse(Long courseId) {
        return (root, query, criteriaBuilder) -> {
            if (courseId == null) return null;
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<StudentEnrollment> enrollment = subquery.from(StudentEnrollment.class);
            subquery.select(enrollment.get("studentUserId"))
                    .where(criteriaBuilder.equal(enrollment.get("courseId"), courseId));
            return root.get("user").get("id").in(subquery);
        };
    }

    /** Matches students who have an enrollment in the given batch. See {@link #hasEnrollmentInCourse}. */
    public static Specification<StudentDetails> hasEnrollmentInBatch(Long batchId) {
        return (root, query, criteriaBuilder) -> {
            if (batchId == null) return null;
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<StudentEnrollment> enrollment = subquery.from(StudentEnrollment.class);
            subquery.select(enrollment.get("studentUserId"))
                    .where(criteriaBuilder.equal(enrollment.get("batchId"), batchId));
            return root.get("user").get("id").in(subquery);
        };
    }

    public static Specification<StudentDetails> isStudent() {
        return (root, query, criteriaBuilder) -> {
            Join<StudentDetails, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("role"), UserRole.STUDENT);
        };
    }
}
