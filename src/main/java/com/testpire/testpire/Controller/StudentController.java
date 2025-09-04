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
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Operations", description = "Student-specific operations")
@CrossOrigin
public class StudentController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student profile", description = "Get current student's profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // Verify the user is a student
            if (currentUser.getRole() != com.testpire.testpire.enums.UserRole.STUDENT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - only students can access this endpoint"));
            }

            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            log.error("Error fetching student profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch profile"));
        }
    }

    @GetMapping("/peers")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student peers", description = "Get all students in the same institute")
    public ResponseEntity<?> getPeers(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // Verify the user is a student
            if (currentUser.getRole() != com.testpire.testpire.enums.UserRole.STUDENT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - only students can access this endpoint"));
            }

            var peers = userService.getUsersByRoleAndInstitute(
                com.testpire.testpire.enums.UserRole.STUDENT, currentUser.getInstituteId());
            
            // Remove the current user from the list
            peers.removeIf(peer -> peer.getId().equals(currentUser.getId()));
            
            return ResponseEntity.ok(peers);
        } catch (Exception e) {
            log.error("Error fetching student peers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch peers"));
        }
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get institute teachers", description = "Get all teachers in the student's institute")
    public ResponseEntity<?> getTeachers(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // Verify the user is a student
            if (currentUser.getRole() != com.testpire.testpire.enums.UserRole.STUDENT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - only students can access this endpoint"));
            }

            var teachers = userService.getUsersByRoleAndInstitute(
                com.testpire.testpire.enums.UserRole.TEACHER, currentUser.getInstituteId());
            
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error fetching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch teachers"));
        }
    }

    @GetMapping("/institute-info")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get institute information", description = "Get information about the student's institute")
    public ResponseEntity<?> getInstituteInfo(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // Verify the user is a student
            if (currentUser.getRole() != com.testpire.testpire.enums.UserRole.STUDENT) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - only students can access this endpoint"));
            }

            // This would need to be implemented to get institute details
            // For now, returning basic info
            return ResponseEntity.ok(Map.of(
                "instituteId", currentUser.getInstituteId(),
                "message", "Institute information endpoint - to be implemented with institute details"
            ));
        } catch (Exception e) {
            log.error("Error fetching institute info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch institute information"));
        }
    }
} 