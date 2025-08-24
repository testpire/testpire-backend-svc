package com.testpire.testpire.Controller;

import com.testpire.testpire.dto.LoginRequest;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username and password")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = cognitoService.login(request.username(), request.password());
            return ResponseEntity.ok(Map.of("token", token, "message", "Login successful"));
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials", "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate session")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            cognitoService.logout(username);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Logout failed", "message", e.getMessage()));
        }
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register student", description = "Register a new student account")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody RegisterRequest request) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExists(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            // Students can only create student accounts
            RegisterRequest studentRequest = new RegisterRequest(
                    request.username(),
                    request.email(),
                    request.password(),
                    request.firstName(),
                    request.lastName(),
                    UserRole.STUDENT,
                    request.instituteId()
            );

            String userId = cognitoService.signUp(studentRequest, UserRole.STUDENT);
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
}
