package com.testpire.testpire.service;

import com.testpire.testpire.entity.RolePermission;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.repository.RolePermissionRepository;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Loads the role -> permission grants from {@code role_permissions} and answers permission checks
 * for {@code AuthorizationAspect}. The mapping is cached in memory at startup and can be refreshed
 * at runtime via {@link #reload()} after the underlying table is edited.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    /** Immutable snapshot, swapped atomically on reload so concurrent reads never see a half-built map. */
    private final AtomicReference<Map<UserRole, Set<Permission>>> cache =
            new AtomicReference<>(Collections.emptyMap());

    @PostConstruct
    public void loadCache() {
        reload();
    }

    /** Rebuilds the role -> permission cache from the database. */
    public void reload() {
        Map<UserRole, Set<Permission>> map = new EnumMap<>(UserRole.class);
        for (UserRole role : UserRole.values()) {
            map.put(role, EnumSet.noneOf(Permission.class));
        }

        int loaded = 0;
        for (RolePermission grant : rolePermissionRepository.findAll()) {
            UserRole role = parseRole(grant.getRole());
            Permission permission = parsePermission(grant.getPermissionCode());
            if (role == null || permission == null) {
                continue; // unknown role/permission code already logged; skip defensively
            }
            map.get(role).add(permission);
            loaded++;
        }

        cache.set(Collections.unmodifiableMap(map));
        log.info("Loaded {} role-permission grants: {}", loaded,
                map.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue().size())
                        .toList());
    }

    /**
     * @return true if {@code role} satisfies the {@code required} permissions. With
     * {@code requireAll=false} (default) any one permission suffices; with {@code requireAll=true}
     * all are required. SUPER_ADMIN is always granted as a safety net against accidental lockout.
     */
    public boolean hasPermission(UserRole role, Permission[] required, boolean requireAll) {
        if (role == UserRole.SUPER_ADMIN) {
            return true;
        }
        if (required == null || required.length == 0) {
            return true;
        }
        Set<Permission> held = getPermissions(role);
        if (requireAll) {
            for (Permission p : required) {
                if (!held.contains(p)) {
                    return false;
                }
            }
            return true;
        }
        for (Permission p : required) {
            if (held.contains(p)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getPermissions(UserRole role) {
        return cache.get().getOrDefault(role, Collections.emptySet());
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Unknown role '{}' in role_permissions; skipping", role);
            return null;
        }
    }

    private Permission parsePermission(String code) {
        try {
            return Permission.valueOf(code);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Unknown permission code '{}' in role_permissions; skipping", code);
            return null;
        }
    }
}
