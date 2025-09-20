package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import java.time.LocalDateTime;

public record UserResponseDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    Boolean enabled,
    Long instituteId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            true,
            user.getInstituteId(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
