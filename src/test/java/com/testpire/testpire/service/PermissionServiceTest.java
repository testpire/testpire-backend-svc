package com.testpire.testpire.service;

import com.testpire.testpire.entity.RolePermission;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.repository.RolePermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private PermissionService service;

    private static void grant(List<RolePermission> rows, UserRole role, Permission... perms) {
        for (Permission p : perms) {
            rows.add(RolePermission.builder().role(role.name()).permissionCode(p.name()).build());
        }
    }

    @BeforeEach
    void setUp() {
        List<RolePermission> rows = new ArrayList<>();
        // ALL_TIER subset
        grant(rows, UserRole.STUDENT, Permission.QUESTION_READ, Permission.COURSE_READ,
                Permission.AUTH_PROFILE, Permission.STUDENT_PROFILE_READ);
        // TEACHER inherits ALL_TIER + STAFF_TIER subset
        grant(rows, UserRole.TEACHER, Permission.QUESTION_READ, Permission.COURSE_READ,
                Permission.AUTH_PROFILE, Permission.STUDENT_PROFILE_READ,
                Permission.QUESTION_CREATE, Permission.QUESTION_UPDATE, Permission.QUESTION_DELETE,
                Permission.USER_CREATE, Permission.TEACHER_READ);
        // INST_ADMIN inherits the above + ADMIN_TIER subset
        grant(rows, UserRole.INST_ADMIN, Permission.QUESTION_READ, Permission.QUESTION_CREATE,
                Permission.TEACHER_CREATE, Permission.STUDENT_CREATE, Permission.INSTITUTE_LIST);
        // SUPER_ADMIN intentionally given only one grant: the safety net should still allow everything
        grant(rows, UserRole.SUPER_ADMIN, Permission.INSTITUTE_CREATE);
        // A junk row must be skipped, not blow up the cache load
        rows.add(RolePermission.builder().role("NONSENSE").permissionCode("NOT_A_PERMISSION").build());

        when(rolePermissionRepository.findAll()).thenReturn(rows);
        service.loadCache();
    }

    @Test
    void anyMatch_grantsWhenRoleHoldsOneRequiredPermission() {
        assertThat(service.hasPermission(UserRole.STUDENT,
                new Permission[]{Permission.QUESTION_READ}, false)).isTrue();
        assertThat(service.hasPermission(UserRole.STUDENT,
                new Permission[]{Permission.QUESTION_CREATE}, false)).isFalse();
        // ANY: holding at least one of several is enough
        assertThat(service.hasPermission(UserRole.STUDENT,
                new Permission[]{Permission.QUESTION_CREATE, Permission.QUESTION_READ}, false)).isTrue();
    }

    @Test
    void requireAll_demandsEveryPermission() {
        assertThat(service.hasPermission(UserRole.TEACHER,
                new Permission[]{Permission.QUESTION_CREATE, Permission.QUESTION_DELETE}, true)).isTrue();
        assertThat(service.hasPermission(UserRole.TEACHER,
                new Permission[]{Permission.QUESTION_CREATE, Permission.INSTITUTE_CREATE}, true)).isFalse();
        // Same set with ANY semantics passes because one is held
        assertThat(service.hasPermission(UserRole.TEACHER,
                new Permission[]{Permission.QUESTION_CREATE, Permission.INSTITUTE_CREATE}, false)).isTrue();
    }

    @Test
    void superAdmin_isAlwaysAllowed_evenWithoutGrant() {
        // RBAC_MANAGE was never granted to SUPER_ADMIN in the mock data
        assertThat(service.hasPermission(UserRole.SUPER_ADMIN,
                new Permission[]{Permission.RBAC_MANAGE}, true)).isTrue();
        assertThat(service.hasPermission(UserRole.SUPER_ADMIN,
                new Permission[]{Permission.QUESTION_DELETE, Permission.INSTITUTE_DELETE}, true)).isTrue();
    }

    @Test
    void emptyRequirement_isAllowed() {
        assertThat(service.hasPermission(UserRole.STUDENT, new Permission[]{}, false)).isTrue();
        assertThat(service.hasPermission(UserRole.STUDENT, null, true)).isTrue();
    }

    @Test
    void seedParity_reflectsRoleHierarchy() {
        // Reads are open to students; writes are not
        assertThat(service.hasPermission(UserRole.STUDENT, new Permission[]{Permission.QUESTION_READ}, false)).isTrue();
        assertThat(service.hasPermission(UserRole.STUDENT, new Permission[]{Permission.QUESTION_CREATE}, false)).isFalse();
        // Teachers can create questions but cannot create teachers
        assertThat(service.hasPermission(UserRole.TEACHER, new Permission[]{Permission.QUESTION_CREATE}, false)).isTrue();
        assertThat(service.hasPermission(UserRole.TEACHER, new Permission[]{Permission.TEACHER_CREATE}, false)).isFalse();
        // Institute admins can create teachers
        assertThat(service.hasPermission(UserRole.INST_ADMIN, new Permission[]{Permission.TEACHER_CREATE}, false)).isTrue();
    }

    @Test
    void getPermissions_returnsCachedSet() {
        assertThat(service.getPermissions(UserRole.STUDENT))
                .containsExactlyInAnyOrder(Permission.QUESTION_READ, Permission.COURSE_READ,
                        Permission.AUTH_PROFILE, Permission.STUDENT_PROFILE_READ);
    }
}
