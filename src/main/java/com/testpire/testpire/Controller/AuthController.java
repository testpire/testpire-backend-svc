package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.LoginRequest;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.dto.request.LoginRequestDto;
import com.testpire.testpire.dto.request.RegisterStudentRequestDto;
import com.testpire.testpire.dto.response.LoginResponseDto;
import com.testpire.testpire.dto.response.LogoutResponseDto;
import com.testpire.testpire.dto.response.ProfileResponseDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.JwksJwtUtil;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
@CrossOrigin
public class AuthController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final UserService userService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username and password")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            String token = cognitoService.login(request.username(), request.password());
            
            // Get user details from local database
            User user = userService.getUserByUsername(request.username());
            
            UserDto userDto = new UserDto(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEnabled(),
                user.getInstituteId()
            );
            
            LoginResponseDto response = LoginResponseDto.success(token, null, 3600L, userDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDto("Login failed: " + e.getMessage(), null, null, null, null, null));
        }
    }

    @PostMapping("/logout")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(summary = "User logout", description = "Logout user and invalidate session")
    public ResponseEntity<LogoutResponseDto> logout() {
        try {
            String username = RequestUtils.getCurrentUsername();
            cognitoService.logout(username);
            return ResponseEntity.ok(LogoutResponseDto.successResponse());
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest()
                    .body(LogoutResponseDto.errorResponse("Logout failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register student", description = "Register a new student account")
    public ResponseEntity<ApiResponseDto> registerStudent(@Valid @RequestBody RegisterStudentRequestDto request) {
        try {
            // Find institute by code
            var institute = instituteService.getInstituteByCode(request.instituteCode());
            if (institute == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute not found with code: " + request.instituteCode()));
            }

            // Create RegisterRequest for Cognito
            RegisterRequest registerRequest = new RegisterRequest(
                    request.username(),
                    request.username(), // email same as username
                    request.password(),
                    request.firstName(),
                    request.lastName(),
                    UserRole.STUDENT,
                    institute.getId()
            );

            String cognitoUserId = cognitoService.signUp(registerRequest, UserRole.STUDENT);
            User createdUser = userService.createUser(registerRequest, UserRole.STUDENT, cognitoUserId, ApplicationConstants.Audit.SELF_REGISTRATION);

            return ResponseEntity.ok(ApiResponseDto.success(
                "Student registration successful",
                Map.of(
                    "userId", createdUser.getId(),
                    "cognitoUserId", cognitoUserId,
                    "instituteId", institute.getId()
                )
            ));
        } catch (Exception e) {
            log.error("Student registration failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(summary = "Get user profile", description = "Get current user's profile")
    public ResponseEntity<ProfileResponseDto> getProfile() {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ProfileResponseDto.error("User not found"));
            }

            // Get complete user details from database
            User currentUser = userService.getUserByCognitoUserId(username);

            UserDto userDto = new UserDto(
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                currentUser.getRole(),
                currentUser.isEnabled(),
                currentUser.getInstituteId()
            );

            return ResponseEntity.ok(ProfileResponseDto.success(userDto));
        } catch (Exception e) {
            log.error("Error fetching user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProfileResponseDto.error("Failed to fetch profile: " + e.getMessage()));
        }
    }
}
