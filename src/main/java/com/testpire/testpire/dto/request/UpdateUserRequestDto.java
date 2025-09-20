package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.UserRole;

public record UpdateUserRequestDto(
    String firstName,
    String lastName,
    UserRole role,
    Long instituteId,
    Boolean enabled
) {}
