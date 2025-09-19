package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Super Admin Controller", description = "Super admin specific operations")
public class SuperAdminController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final UserService userService;

    // ========== GENERAL USER MANAGEMENT ==========

    @GetMapping("/users")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Get all users", description = "Get all users (SUPER_ADMIN only)")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch users"));
        }
    }

    @GetMapping("/users/{id}")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error fetching user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch user"));
        }
    }

    // ========== SYSTEM OVERVIEW ==========

    @GetMapping("/dashboard")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Get system dashboard", description = "Get system overview and statistics")
    public ResponseEntity<?> getSystemDashboard() {
        try {
            // Get counts for different user types
            long totalUsers = userService.getAllActiveUsers().size();
            long totalTeachers = userService.getUsersByRole(UserRole.TEACHER).size();
            long totalStudents = userService.getUsersByRole(UserRole.STUDENT).size();
            long totalInstAdmins = userService.getUsersByRole(UserRole.INST_ADMIN).size();
            
            // Get institute count
            long totalInstitutes = instituteService.getAllActiveInstitutes().size();

            return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalTeachers", totalTeachers,
                "totalStudents", totalStudents,
                "totalInstAdmins", totalInstAdmins,
                "totalInstitutes", totalInstitutes,
                "message", "System dashboard retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error fetching system dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch system dashboard"));
        }
    }

}
