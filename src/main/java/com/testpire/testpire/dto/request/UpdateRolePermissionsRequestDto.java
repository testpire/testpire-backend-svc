package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Replaces the full set of permissions granted to a role. The {@code permissions} list is the
 * complete desired state: any grant not present is removed, any new code is added. Each entry must
 * be a valid {@link com.testpire.testpire.enums.Permission} code (e.g. {@code QUESTION_CREATE}).
 * An empty list clears all of the role's permissions.
 */
public record UpdateRolePermissionsRequestDto(
    @NotNull(message = "permissions list is required (may be empty to clear all grants)")
    List<String> permissions
) {}
