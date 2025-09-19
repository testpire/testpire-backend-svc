package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inst-admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Institute Admin", description = "Institute admin operations - INST_ADMIN and SUPER_ADMIN")
public class InstAdminController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;

    @PostMapping("/register/teacher")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Register teacher", description = "Register a new teacher (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<?> registerTeacher(@Valid @RequestBody RegisterRequest request) {
        try {
            // Validate that the user is trying to create a teacher
            if (request.role() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating teachers"));
            }

            // Validate institute exists
            if (!instituteService.instituteExistsById(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            String userId = cognitoService.signUp(request, UserRole.TEACHER);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Teacher registered successfully"
            ));
        } catch (Exception e) {
            log.error("Teacher registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Teacher registration failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/register/student")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Register student", description = "Register a new student (SUPER_ADMIN, INST_ADMIN, or TEACHER)")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody RegisterRequest request) {
        try {
            // Validate that the user is trying to create a student
            if (request.role() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating students"));
            }

            // Validate institute exists
            if (!instituteService.instituteExistsById(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            String userId = cognitoService.signUp(request, UserRole.STUDENT);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Student registered successfully"
            ));
        } catch (Exception e) {
            log.error("Student registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Student registration failed", "message", e.getMessage()));
        }
    }

    @GetMapping("/teachers")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Get teachers", description = "Get all teachers in the institute (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<?> getTeachers() {
        try {
            // This would need to be implemented in CognitoService to get users by role and institute
            // For now, returning a placeholder
            return ResponseEntity.ok(Map.of("message", "Teacher list endpoint - to be implemented"));
        } catch (Exception e) {
            log.error("Error fetching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch teachers"));
        }
    }

    @GetMapping("/students")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get students", description = "Get all students in the institute (SUPER_ADMIN, INST_ADMIN, or TEACHER)")
    public ResponseEntity<?> getStudents() {
        try {
            // This would need to be implemented in CognitoService to get users by role and institute
            // For now, returning a placeholder
            return ResponseEntity.ok(Map.of("message", "Student list endpoint - to be implemented"));
        } catch (Exception e) {
            log.error("Error fetching students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch students"));
        }
    }
} 