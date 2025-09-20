package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Institute;
import java.time.LocalDateTime;

public record InstituteResponseDto(
    Long id,
    String name,
    String code,
    String description,
    String email,
    String phone,
    String website,
    String address,
    String city,
    String state,
    String country,
    String postalCode,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {
    public static InstituteResponseDto fromEntity(Institute institute) {
        return new InstituteResponseDto(
            institute.getId(),
            institute.getName(),
            institute.getCode(),
            institute.getDescription(),
            institute.getEmail(),
            institute.getPhone(),
            institute.getWebsite(),
            institute.getAddress(),
            institute.getCity(),
            institute.getState(),
            institute.getCountry(),
            institute.getPostalCode(),
            true,
            institute.getCreatedAt(),
            institute.getUpdatedAt(),
            institute.getCreatedBy(),
            institute.getUpdatedBy()
        );
    }
}
