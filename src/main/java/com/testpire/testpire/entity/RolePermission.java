package com.testpire.testpire.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single role -> permission grant row from the {@code role_permissions} table.
 * Loaded in bulk and cached by {@code PermissionService}; not edited through JPA on the request path.
 */
@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Role name, matching {@link com.testpire.testpire.enums.UserRole}. */
    @Column(nullable = false, length = 32)
    private String role;

    /** Permission code, matching {@link com.testpire.testpire.enums.Permission}. */
    @Column(name = "permission_code", nullable = false, length = 64)
    private String permissionCode;
}
