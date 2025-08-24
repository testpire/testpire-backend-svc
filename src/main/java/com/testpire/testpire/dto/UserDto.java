package com.testpire.testpire.dto;

import com.testpire.testpire.enums.UserRole;

// UserDto.java
public record UserDto(
    String username,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    boolean enabled,
    String instituteId
) {}
