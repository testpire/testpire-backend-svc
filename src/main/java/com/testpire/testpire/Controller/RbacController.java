package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.UpdateRolePermissionsRequestDto;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Inspect and refresh the role -> permission mapping that backs fine-grained authorization.
 * The grants themselves live in the {@code role_permissions} table; edit that table directly and
 * then call {@code POST /api/rbac/reload} to apply the change without a redeploy.
 */
@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RBAC", description = "View and reload role-permission mappings")
@SecurityRequirement(name = "Bearer Authentication")
public class RbacController {

    private final PermissionService permissionService;

    @GetMapping("/permissions")
    @RequirePermission(Permission.RBAC_MANAGE)
    @Operation(summary = "List the full permission catalog")
    public ResponseEntity<?> listPermissions() {
        List<Map<String, String>> catalog = Arrays.stream(Permission.values())
                .map(p -> Map.of("code", p.name(), "description", p.getDescription()))
                .toList();
        return ResponseEntity.ok(Map.of("permissions", catalog, "count", catalog.size()));
    }

    @GetMapping("/roles/{role}/permissions")
    @RequirePermission(Permission.RBAC_MANAGE)
    @Operation(summary = "List the permissions currently granted to a role")
    public ResponseEntity<?> getRolePermissions(@PathVariable String role) {
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid role", "message", "Unknown role: " + role));
        }
        List<String> permissions = permissionService.getPermissions(userRole).stream()
                .map(Permission::name)
                .sorted(Comparator.naturalOrder())
                .toList();
        return ResponseEntity.ok(Map.of("role", userRole.name(), "permissions", permissions,
                "count", permissions.size()));
    }

    @PutMapping("/roles/{role}/permissions")
    @RequirePermission(Permission.RBAC_MANAGE)
    @Operation(summary = "Replace the full set of permissions granted to a role")
    public ResponseEntity<?> updateRolePermissions(
            @PathVariable String role,
            @Valid @RequestBody UpdateRolePermissionsRequestDto request) {
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid role", "message", "Unknown role: " + role));
        }

        // SUPER_ADMIN is always-allowed in code; its stored grants are ignored. Reject edits so the UI
        // can't be misled into thinking they took effect (and to keep the lockout safety net intact).
        if (userRole == UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Immutable role",
                    "message", "SUPER_ADMIN permissions cannot be edited; the role is always granted all permissions"));
        }

        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        List<String> unknown = new ArrayList<>();
        for (String code : new LinkedHashSet<>(request.permissions())) {
            try {
                permissions.add(Permission.valueOf(code));
            } catch (IllegalArgumentException | NullPointerException e) {
                unknown.add(code);
            }
        }
        if (!unknown.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Invalid permission code(s)",
                    "message", "Unknown permission code(s): " + unknown));
        }

        permissionService.setPermissions(userRole, permissions);
        List<String> applied = permissions.stream()
                .map(Permission::name)
                .sorted(Comparator.naturalOrder())
                .toList();
        log.info("Permissions for role {} updated to {} grants", userRole, applied.size());
        return ResponseEntity.ok(Map.of("role", userRole.name(), "permissions", applied,
                "count", applied.size()));
    }

    @PostMapping("/reload")
    @RequirePermission(Permission.RBAC_MANAGE)
    @Operation(summary = "Reload the role-permission cache from the database")
    public ResponseEntity<?> reload() {
        permissionService.reload();
        log.info("RBAC role-permission cache reloaded on request");
        return ResponseEntity.ok(Map.of("message", "Role-permission cache reloaded"));
    }
}
