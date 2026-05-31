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
    /**
     * Minimal projection for the UNAUTHENTICATED /institutes/code/{code} lookup (used by the
     * signup-by-code flow). Exposes only id, name, code and active status — never contact details
     * (email/phone/address) which would otherwise allow anonymous tenant data harvesting.
     */
    public static InstituteResponseDto publicView(Institute institute) {
        return new InstituteResponseDto(
            institute.getId(),
            institute.getName(),
            institute.getCode(),
            null, null, null, null, null, null, null, null, null, // description..postalCode
            true,                                                  // active
            null, null, null, null                                 // createdAt, updatedAt, createdBy, updatedBy
        );
    }

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
