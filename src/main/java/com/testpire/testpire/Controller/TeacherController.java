package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Operations", description = "Teacher-specific operations")
public class TeacherController {

    private final UserService userService;

    @GetMapping("/dashboard")
    @RequireRole(UserRole.TEACHER)
    @Operation(summary = "Get teacher dashboard", description = "Get teacher's dashboard with student information")
    public ResponseEntity<?> getTeacherDashboard() {
        try {
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            // Get student count for the teacher's institute
            var students = userService.getUsersByRoleAndInstitute(
                UserRole.STUDENT, currentUser.instituteId());
            
            return ResponseEntity.ok(Map.of(
                "instituteId", currentUser.instituteId(),
                "totalStudents", students.size(),
                "message", "Teacher dashboard retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error fetching teacher dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch dashboard"));
        }
    }

    @GetMapping("/profile")
    @RequireRole(UserRole.TEACHER)
    @Operation(summary = "Get teacher profile", description = "Get current teacher's profile")
    public ResponseEntity<?> getProfile() {
        try {
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            log.error("Error fetching teacher profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile"));
        }
    }
} 