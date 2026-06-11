package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import java.time.Instant;

public record UserResponseDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    Long instituteId,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getInstituteId(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
