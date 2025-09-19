package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.dto.UserDto;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Centralized user management operations")
public class UserManagementController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final UserService userService;

    // ========== USER REGISTRATION ==========

    @PostMapping("/register")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Register user", description = "Register a new user based on role and permissions")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            // Get current user's context
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            // Validate institute exists
            if (!instituteService.instituteExistsById(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute not found with ID: " + request.instituteId()));
            }

            // Role-based permission validation
            if (!canCreateUser(currentUser, request.role(), request.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Insufficient permissions to create this user type"));
            }

            // Create user in Cognito
            String cognitoUserId = cognitoService.signUp(request, request.role());
            
            // Create user in local database
            User createdUser = userService.createUser(request, request.role(), cognitoUserId, currentUser.username());

            return ResponseEntity.ok(Map.of(
                "userId", createdUser.getId(),
                "cognitoUserId", cognitoUserId,
                "message", request.role() + " registered successfully"
            ));
        } catch (Exception e) {
            log.error("User registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Registration failed", "message", e.getMessage()));
        }
    }

    // ========== USER RETRIEVAL ==========

    @GetMapping("/{role}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get users by role", description = "Get users by role with institute-based filtering")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            List<User> users;
            if (currentUser.role() == UserRole.SUPER_ADMIN) {
                // SUPER_ADMIN can see all users of any role
                users = userService.getUsersByRole(userRole);
            } else {
                // INST_ADMIN and TEACHER can only see users in their institute
                users = userService.getUsersByRoleAndInstitute(userRole, currentUser.instituteId());
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users by role", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch users"));
        }
    }

    @GetMapping("/{role}/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Search users by role", description = "Search users by role and institute")
    public ResponseEntity<?> searchUsersByRole(@PathVariable String role,
                                            @RequestParam String searchTerm) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            List<User> users;
            if (currentUser.role() == UserRole.SUPER_ADMIN) {
                // SUPER_ADMIN can search all users
                users = userService.searchUsersByRoleAndInstitute(userRole, null, searchTerm);
            } else {
                // INST_ADMIN and TEACHER can only search in their institute
                users = userService.searchUsersByRoleAndInstitute(userRole, currentUser.instituteId(), searchTerm);
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error searching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search users"));
        }
    }

    @GetMapping("/{role}/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get user by ID", description = "Get user details by ID with permission check")
    public ResponseEntity<?> getUserById(@PathVariable String role,
                                       @PathVariable Long id) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            User targetUser = userService.getUserById(id);
            
            // Verify the target user has the expected role
            if (targetUser.getRole() != userRole) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User is not a " + role));
            }

            // Permission check
            if (currentUser.role() != UserRole.SUPER_ADMIN && 
                !targetUser.getInstituteId().equals(currentUser.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - user not in your institute"));
            }

            return ResponseEntity.ok(targetUser);
        } catch (Exception e) {
            log.error("Error fetching user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch user"));
        }
    }

    // ========== USER UPDATES ==========

    @PutMapping("/{role}/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Update user", description = "Update user details with permission check")
    public ResponseEntity<?> updateUser(@PathVariable String role,
                                      @PathVariable Long id,
                                      @Valid @RequestBody RegisterRequest request) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            User existingUser = userService.getUserById(id);
            
            // Verify the target user has the expected role
            if (existingUser.getRole() != userRole) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User is not a " + role));
            }

            // Permission check
            if (currentUser.role() != UserRole.SUPER_ADMIN && 
                !existingUser.getInstituteId().equals(currentUser.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - user not in your institute"));
            }

            User updatedUser = userService.updateUser(id, request, currentUser.username());
            return ResponseEntity.ok(Map.of(
                "message", role + " updated successfully",
                "userId", updatedUser.getId()
            ));
        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update user", "message", e.getMessage()));
        }
    }

    // ========== USER DELETION ==========

    @DeleteMapping("/{role}/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Delete user", description = "Deactivate user with permission check")
    public ResponseEntity<?> deleteUser(@PathVariable String role,
                                      @PathVariable Long id) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
            }

            User existingUser = userService.getUserById(id);
            
            // Verify the target user has the expected role
            if (existingUser.getRole() != userRole) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User is not a " + role));
            }

            // Permission check
            if (currentUser.role() != UserRole.SUPER_ADMIN && 
                !existingUser.getInstituteId().equals(currentUser.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - user not in your institute"));
            }

            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", role + " deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to delete user", "message", e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    private boolean canCreateUser(UserDto currentUser, UserRole targetRole, Long targetInstituteId) {
        // SUPER_ADMIN can create any user type in any institute
        if (currentUser.role() == UserRole.SUPER_ADMIN) {
            return true;
        }

        // INST_ADMIN can create TEACHER and STUDENT in their own institute
        if (currentUser.role() == UserRole.INST_ADMIN) {
            return (targetRole == UserRole.TEACHER || targetRole == UserRole.STUDENT) &&
                   targetInstituteId.equals(currentUser.instituteId());
        }

        // TEACHER can only create STUDENT in their own institute
        if (currentUser.role() == UserRole.TEACHER) {
            return targetRole == UserRole.STUDENT && targetInstituteId.equals(currentUser.instituteId());
        }

        return false;
    }
} 