package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.entity.User;
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
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Operations", description = "Student-specific operations")
public class StudentController {

    private final UserService userService;

    @GetMapping("/profile")
    @RequireRole(UserRole.STUDENT)
    @Operation(summary = "Get student profile", description = "Get current student's profile")
    public ResponseEntity<?> getProfile() {
        try {
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            log.error("Error fetching student profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile"));
        }
    }

    @GetMapping("/peers")
    @RequireRole(UserRole.STUDENT)
    @Operation(summary = "Get student peers", description = "Get all students in the same institute")
    public ResponseEntity<?> getPeers() {
        try {
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            var peers = userService.getUsersByRoleAndInstitute(
                UserRole.STUDENT, currentUser.instituteId());
            
            return ResponseEntity.ok(peers);
        } catch (Exception e) {
            log.error("Error fetching student peers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch peers"));
        }
    }

    @GetMapping("/teachers")
    @RequireRole(UserRole.STUDENT)
    @Operation(summary = "Get institute teachers", description = "Get all teachers in the student's institute")
    public ResponseEntity<?> getTeachers() {
        try {
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            var teachers = userService.getUsersByRoleAndInstitute(
                UserRole.TEACHER, currentUser.instituteId());
            
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error fetching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch teachers"));
        }
    }

    @GetMapping("/institute-info")
    @RequireRole(UserRole.STUDENT)
    @Operation(summary = "Get institute information", description = "Get information about the student's institute")
    public ResponseEntity<?> getInstituteInfo() {
        try {
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            // This would need to be implemented to get institute details
            // For now, returning basic info
            return ResponseEntity.ok(Map.of(
                "instituteId", currentUser.instituteId(),
                "message", "Institute information endpoint - to be implemented with institute details"
            ));
        } catch (Exception e) {
            log.error("Error fetching institute info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch institute information"));
        }
    }
} 