package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.UserRole;

public record UpdateTeacherRequestDto(
    String firstName,
    String lastName,
    String phone,
    String department,
    String subject,
    String qualification,
    Integer experienceYears,
    String specialization,
    String bio,
    Long instituteId,
    Boolean enabled
) {}
