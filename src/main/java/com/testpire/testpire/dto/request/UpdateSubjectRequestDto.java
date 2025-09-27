package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSubjectRequestDto(
        @Size(max = 100, message = "Subject name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 20, message = "Subject code must not exceed 20 characters")
        String code,

        String duration,
        Integer credits,
        String prerequisites,
        Boolean active
) {}


