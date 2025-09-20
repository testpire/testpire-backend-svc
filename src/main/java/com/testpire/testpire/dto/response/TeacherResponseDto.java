package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import java.time.LocalDateTime;

public record TeacherResponseDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    Boolean enabled,
    Long instituteId,
    String phone,
    String department,
    String subject,
    String qualification,
    Integer experienceYears,
    String specialization,
    String bio,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TeacherResponseDto fromEntity(User user, com.testpire.testpire.entity.TeacherDetails teacherDetails) {
        return new TeacherResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.isEnabled(),
            user.getInstituteId(),
            teacherDetails != null ? teacherDetails.getPhone() : null,
            teacherDetails != null ? teacherDetails.getDepartment() : null,
            teacherDetails != null ? teacherDetails.getSubject() : null,
            teacherDetails != null ? teacherDetails.getQualification() : null,
            teacherDetails != null ? teacherDetails.getExperienceYears() : null,
            teacherDetails != null ? teacherDetails.getSpecialization() : null,
            teacherDetails != null ? teacherDetails.getBio() : null,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
