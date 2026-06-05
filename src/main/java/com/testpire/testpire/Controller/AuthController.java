package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.dto.request.ConfirmForgotPasswordRequestDto;
import com.testpire.testpire.dto.request.ForgotPasswordRequestDto;
import com.testpire.testpire.dto.request.LoginRequestDto;
import com.testpire.testpire.dto.request.SetPasswordRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.LoginResponseDto;
import com.testpire.testpire.dto.response.LogoutResponseDto;
import com.testpire.testpire.dto.response.ProfileResponseDto;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.JwksJwtUtil;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @Operation(summary = "User login", description = "Returns a token on success, or a challenge name + session if the password must be changed first.")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            Map<String, String> result = cognitoService.login(request.username(), request.password());

            // Cognito returned a challenge (e.g. first-time password change required)
            if (result.containsKey("challengeName")) {
                return ResponseEntity.ok(LoginResponseDto.challenge(
                        result.get("challengeName"), result.get("session")));
            }

            User user = userService.getUserByUsername(request.username());
            UserDto userDto = new UserDto(
                    user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                    user.getRole(), user.isEnabled(), user.getInstituteId());

            return ResponseEntity.ok(LoginResponseDto.success(result.get("token"), null, 3600L, userDto));

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDto("Login failed: " + e.getMessage(),
                            null, null, null, null, null, null, null));
        }
    }

    @PostMapping("/set-password")
    @Operation(summary = "Set new password",
               description = "Called after first login when Cognito returns NEW_PASSWORD_REQUIRED. " +
                             "Provide the session from the login response and the desired new password.")
    public ResponseEntity<LoginResponseDto> setPassword(@Valid @RequestBody SetPasswordRequestDto request) {
        try {
            String token = cognitoService.setNewPassword(
                    request.username(), request.session(), request.newPassword());

            User user = userService.getUserByUsername(request.username());
            UserDto userDto = new UserDto(
                    user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                    user.getRole(), user.isEnabled(), user.getInstituteId());

            return ResponseEntity.ok(LoginResponseDto.success(token, null, 3600L, userDto));

        } catch (Exception e) {
            log.error("Set password failed for: {}", request.username(), e);
            return ResponseEntity.badRequest()
                    .body(new LoginResponseDto("Set password failed: " + e.getMessage(),
                            null, null, null, null, null, null, null));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password",
               description = "Sends a password-reset verification code to the user's email.")
    public ResponseEntity<ApiResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        try {
            cognitoService.forgotPassword(request.username());
            return ResponseEntity.ok(ApiResponseDto.success(
                    "Password reset code sent to your email", null));
        } catch (Exception e) {
            log.error("Forgot password failed for: {}", request.username(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Forgot password failed: " + e.getMessage()));
        }
    }

    @PostMapping("/confirm-forgot-password")
    @Operation(summary = "Confirm forgot password",
               description = "Resets the password using the verification code sent to the user's email.")
    public ResponseEntity<ApiResponseDto> confirmForgotPassword(
            @Valid @RequestBody ConfirmForgotPasswordRequestDto request) {
        try {
            cognitoService.confirmForgotPassword(
                    request.username(), request.confirmationCode(), request.newPassword());
            return ResponseEntity.ok(ApiResponseDto.success("Password reset successfully", null));
        } catch (Exception e) {
            log.error("Confirm forgot password failed for: {}", request.username(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Password reset failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @RequirePermission(Permission.AUTH_LOGOUT)
    @Operation(summary = "User logout", description = "Logout user and invalidate all sessions")
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

    @GetMapping("/profile")
    @RequirePermission(Permission.AUTH_PROFILE)
    @Operation(summary = "Get user profile", description = "Get current user's profile")
    public ResponseEntity<ProfileResponseDto> getProfile() {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ProfileResponseDto.error("User not found"));
            }

            User currentUser = userService.getUserByCognitoUserId(username);
            UserDto userDto = new UserDto(
                    currentUser.getUsername(), currentUser.getEmail(),
                    currentUser.getFirstName(), currentUser.getLastName(),
                    currentUser.getRole(), currentUser.isEnabled(), currentUser.getInstituteId());

            return ResponseEntity.ok(ProfileResponseDto.success(userDto));
        } catch (Exception e) {
            log.error("Error fetching user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProfileResponseDto.error("Failed to fetch profile: " + e.getMessage()));
        }
    }
}
