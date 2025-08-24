package com.testpire.testpire.Controller;

import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/super-admin")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Super Admin Controller", description = "CRUD for institute admin, teacher or student")
public class SuperAdminController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register teacher", description = "Register a new teacher (SUPER_ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Teacher registered successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/register/teacher")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerTeacher(@Valid @RequestBody RegisterRequest request,
        Authentication authentication) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExists(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            // Validate that the user is trying to create a teacher
            if (request.role() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating teachers"));
            }

            String userId = cognitoService.signUp(request, UserRole.TEACHER);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Teacher registered successfully"
            ));
        } catch (Exception e) {
            log.error("Teacher registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Registration failed", "message", e.getMessage()));
        }
    }

    @Operation(summary = "Register student", description = "Register a new student (SUPER_ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Student registered successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/register/student")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerStudentAsAdmin(@Valid @RequestBody RegisterRequest request,
        Authentication authentication) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExists(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            // Validate that the user is trying to create a student
            if (request.role() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating students"));
            }

            String userId = cognitoService.signUp(request, UserRole.STUDENT);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Student registered successfully"
            ));
        } catch (Exception e) {
            log.error("Student registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Registration failed", "message", e.getMessage()));
        }
    }

    @Operation(summary = "Register institute admin", description = "Register a new institute admin (SUPER_ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Institute admin registered successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/register/inst-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerInstAdmin(@Valid @RequestBody RegisterRequest request,
        Authentication authentication) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExists(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            // Validate that the user is trying to create an institute admin
            if (request.role() != UserRole.INST_ADMIN) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating institute admins"));
            }

            String userId = cognitoService.signUp(request, UserRole.INST_ADMIN);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Institute admin registered successfully"
            ));
        } catch (Exception e) {
            log.error("Institute admin registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Registration failed", "message", e.getMessage()));
        }
    }

    private UserRole getUserRoleFromAuthentication(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.replace("ROLE_", ""))
            .map(UserRole::valueOf)
            .findFirst()
            .orElse(UserRole.STUDENT);
    }
}
