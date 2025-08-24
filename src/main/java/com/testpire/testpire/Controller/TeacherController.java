package com.testpire.testpire.Controller;

import com.testpire.testpire.entity.User;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Operations", description = "Teacher-specific operations")
public class TeacherController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get teacher dashboard", description = "Get teacher's dashboard with student information")
    public ResponseEntity<?> getTeacherDashboard(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // Get student count for the teacher's institute
            var students = userService.getUsersByRoleAndInstitute(
                com.testpire.testpire.enums.UserRole.STUDENT, currentUser.getInstituteId());
            
            return ResponseEntity.ok(Map.of(
                "teacherId", currentUser.getId(),
                "instituteId", currentUser.getInstituteId(),
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
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get teacher profile", description = "Get current teacher's profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            log.error("Error fetching teacher profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile"));
        }
    }
} 