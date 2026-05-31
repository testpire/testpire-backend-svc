package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import java.time.LocalDateTime;

public record StudentResponseDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    Boolean enabled,
    Long instituteId,
    String phone,
    String course,
    Integer yearOfStudy,
    String rollNumber,
    String parentName,
    String parentPhone,
    String parentEmail,
    String address,
    LocalDateTime dateOfBirth,
    String bloodGroup,
    String emergencyContact,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Minimal projection for peer listings (one student viewing classmates). Excludes PII —
     * email, username, phone, parent contacts, address, DOB, blood group, emergency contact —
     * exposing only name and basic academic placement.
     */
    public static StudentResponseDto peerView(User user, com.testpire.testpire.entity.StudentDetails studentDetails) {
        return new StudentResponseDto(
            user.getId(),
            null,                 // username
            null,                 // email
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            null,                 // enabled
            user.getInstituteId(),
            null,                 // phone
            studentDetails != null ? studentDetails.getCourse() : null,
            studentDetails != null ? studentDetails.getYearOfStudy() : null,
            studentDetails != null ? studentDetails.getRollNumber() : null,
            null,                 // parentName
            null,                 // parentPhone
            null,                 // parentEmail
            null,                 // address
            null,                 // dateOfBirth
            null,                 // bloodGroup
            null,                 // emergencyContact
            null,                 // createdAt
            null                  // updatedAt
        );
    }

    public static StudentResponseDto fromEntity(User user, com.testpire.testpire.entity.StudentDetails studentDetails) {
        return new StudentResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.isEnabled(),
            user.getInstituteId(),
            studentDetails != null ? studentDetails.getPhone() : null,
            studentDetails != null ? studentDetails.getCourse() : null,
            studentDetails != null ? studentDetails.getYearOfStudy() : null,
            studentDetails != null ? studentDetails.getRollNumber() : null,
            studentDetails != null ? studentDetails.getParentName() : null,
            studentDetails != null ? studentDetails.getParentPhone() : null,
            studentDetails != null ? studentDetails.getParentEmail() : null,
            studentDetails != null ? studentDetails.getAddress() : null,
            studentDetails != null ? studentDetails.getDateOfBirth() : null,
            studentDetails != null ? studentDetails.getBloodGroup() : null,
            studentDetails != null ? studentDetails.getEmergencyContact() : null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
