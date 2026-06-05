package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    @PostMapping("/reload")
    @RequirePermission(Permission.RBAC_MANAGE)
    @Operation(summary = "Reload the role-permission cache from the database")
    public ResponseEntity<?> reload() {
        permissionService.reload();
        log.info("RBAC role-permission cache reloaded on request");
        return ResponseEntity.ok(Map.of("message", "Role-permission cache reloaded"));
    }
}
