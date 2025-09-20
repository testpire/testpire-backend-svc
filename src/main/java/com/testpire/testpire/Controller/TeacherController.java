package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.dto.request.CreateTeacherRequestDto;
import com.testpire.testpire.dto.request.UpdateTeacherRequestDto;
import com.testpire.testpire.dto.response.TeacherResponseDto;
import com.testpire.testpire.dto.response.TeacherListResponseDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.service.TeacherDetailsService;
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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Management", description = "Teacher CRUD operations and management")
public class TeacherController {

    private final UserService userService;
    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final TeacherDetailsService teacherDetailsService;

    // ==================== TEACHER CRUD OPERATIONS ====================

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Create teacher", description = "Create a new teacher (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> createTeacher(@Valid @RequestBody CreateTeacherRequestDto request) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExistsById(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute not found with ID: " + request.instituteId()));
            }

            // Convert to RegisterRequest for Cognito
            RegisterRequest registerRequest = new RegisterRequest(
                request.username(),
                request.username(), // email same as username
                request.password(),
                request.firstName(),
                request.lastName(),
                UserRole.TEACHER,
                request.instituteId()
            );

            // Create user in Cognito
            String cognitoUserId = cognitoService.signUp(registerRequest, UserRole.TEACHER);
            
            // Create user in local database
            User createdUser = userService.createUser(registerRequest, UserRole.TEACHER, cognitoUserId, RequestUtils.getCurrentUsername());
            
            // Create teacher details
            com.testpire.testpire.entity.TeacherDetails teacherDetails = teacherDetailsService.createTeacherDetails(
                createdUser,
                request.phone(),
                request.department(),
                request.subject()
            );

            TeacherResponseDto response = TeacherResponseDto.fromEntity(createdUser, teacherDetails);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success("Teacher created successfully", response)
            );
        } catch (Exception e) {
            log.error("Error creating teacher", e);
            return ResponseEntity.badRequest()
                .body(ApiResponseDto.error("Failed to create teacher: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get teacher by ID", description = "Get teacher details by ID")
    public ResponseEntity<ApiResponseDto> getTeacherById(@PathVariable Long id) {
        try {
            User teacher = userService.getUserById(id);
            
            // Verify it's a teacher
            if (teacher.getRole() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a teacher"));
            }

            // Get teacher details
            com.testpire.testpire.entity.TeacherDetails teacherDetails = teacherDetailsService.getTeacherDetailsByUser(teacher).orElse(null);
            
            TeacherResponseDto response = TeacherResponseDto.fromEntity(teacher, teacherDetails);
            return ResponseEntity.ok(ApiResponseDto.success("Teacher retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error fetching teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch teacher: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Update teacher", description = "Update teacher details (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> updateTeacher(@PathVariable Long id, @Valid @RequestBody UpdateTeacherRequestDto request) {
        try {
            User teacher = userService.getUserById(id);
            
            // Verify it's a teacher
            if (teacher.getRole() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a teacher"));
            }

            // Update user fields if provided
            if (request.firstName() != null) {
                teacher.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                teacher.setLastName(request.lastName());
            }
            if (request.instituteId() != null) {
                // Validate institute exists
                if (!instituteService.instituteExistsById(request.instituteId())) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponseDto.error("Institute not found with ID: " + request.instituteId()));
                }
                teacher.setInstituteId(request.instituteId());
            }
            if (request.enabled() != null) {
                teacher.setEnabled(request.enabled());
            }

            User updatedTeacher = userService.updateUser(teacher);
            
            // Update teacher details
            com.testpire.testpire.entity.TeacherDetails teacherDetails = teacherDetailsService.updateTeacherDetails(
                teacher,
                request.phone(),
                request.department(),
                request.subject(),
                request.qualification(),
                request.experienceYears(),
                request.specialization(),
                request.bio()
            );
            
            TeacherResponseDto response = TeacherResponseDto.fromEntity(updatedTeacher, teacherDetails);
            
            return ResponseEntity.ok(ApiResponseDto.success("Teacher updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update teacher: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Delete teacher", description = "Delete teacher (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> deleteTeacher(@PathVariable Long id) {
        try {
            User teacher = userService.getUserById(id);
            
            // Verify it's a teacher
            if (teacher.getRole() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a teacher"));
            }

            // Delete teacher details first
            teacherDetailsService.deleteTeacherDetails(id);
            
            // Then delete the user
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponseDto.success("Teacher deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to delete teacher: " + e.getMessage()));
        }
    }

    // ==================== TEACHER LISTING OPERATIONS ====================

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get all teachers", description = "Get all teachers with optional filtering")
    public ResponseEntity<TeacherListResponseDto> getAllTeachers(
            @RequestParam(required = false) Long instituteId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String subject) {
        try {
            List<com.testpire.testpire.entity.TeacherDetails> teacherDetailsList;
            
            if (instituteId != null) {
                // Get teachers by institute
                teacherDetailsList = teacherDetailsService.getTeachersByInstitute(instituteId);
            } else {
                // Get all teachers
                List<User> teachers = userService.getUsersByRole(UserRole.TEACHER);
                teacherDetailsList = teachers.stream()
                    .map(teacher -> teacherDetailsService.getTeacherDetailsByUser(teacher).orElse(null))
                    .filter(details -> details != null)
                    .toList();
            }

            // Apply additional filters
            List<TeacherResponseDto> teacherDtos = teacherDetailsList.stream()
                .filter(details -> department == null || department.equals(details.getDepartment()))
                .filter(details -> subject == null || subject.equals(details.getSubject()))
                .map(details -> TeacherResponseDto.fromEntity(details.getUser(), details))
                .toList();

            TeacherListResponseDto response = TeacherListResponseDto.success(
                teacherDtos, 
                teacherDtos.size(), 
                0, 
                teacherDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TeacherListResponseDto.error("Failed to fetch teachers: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get teachers by institute", description = "Get all teachers in a specific institute")
    public ResponseEntity<TeacherListResponseDto> getTeachersByInstitute(@PathVariable Long instituteId) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(TeacherListResponseDto.error("Institute not found with ID: " + instituteId));
            }

            List<com.testpire.testpire.entity.TeacherDetails> teacherDetailsList = teacherDetailsService.getTeachersByInstitute(instituteId);
            
            List<TeacherResponseDto> teacherDtos = teacherDetailsList.stream()
                .map(details -> TeacherResponseDto.fromEntity(details.getUser(), details))
                .toList();

            TeacherListResponseDto response = TeacherListResponseDto.success(
                teacherDtos, 
                teacherDtos.size(), 
                0, 
                teacherDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching teachers by institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TeacherListResponseDto.error("Failed to fetch teachers: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Search teachers", description = "Search teachers by name, department, or subject")
    public ResponseEntity<TeacherListResponseDto> searchTeachers(
            @RequestParam String query,
            @RequestParam(required = false) Long instituteId) {
        try {
            List<com.testpire.testpire.entity.TeacherDetails> teacherDetailsList;
            
            if (instituteId != null) {
                // Search within specific institute
                teacherDetailsList = teacherDetailsService.searchTeachersByInstitute(instituteId, query);
            } else {
                // Search all teachers
                teacherDetailsList = teacherDetailsService.searchTeachers(query);
            }

            List<TeacherResponseDto> teacherDtos = teacherDetailsList.stream()
                .map(details -> TeacherResponseDto.fromEntity(details.getUser(), details))
                .toList();

            TeacherListResponseDto response = TeacherListResponseDto.success(
                teacherDtos, 
                teacherDtos.size(), 
                0, 
                teacherDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TeacherListResponseDto.error("Failed to search teachers: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @RequireRole(UserRole.TEACHER)
    @Operation(summary = "Get teacher profile", description = "Get current teacher's profile")
    public ResponseEntity<ApiResponseDto> getTeacherProfile() {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User teacher = userService.getUserByCognitoUserId(username);

            // Get teacher details
            com.testpire.testpire.entity.TeacherDetails teacherDetails = teacherDetailsService.getTeacherDetailsByUser(teacher).orElse(null);
            
            TeacherResponseDto response = TeacherResponseDto.fromEntity(teacher, teacherDetails);
            return ResponseEntity.ok(ApiResponseDto.success("Teacher profile retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error fetching teacher profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch teacher profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @RequireRole(UserRole.TEACHER)
    @Operation(summary = "Update teacher profile", description = "Update current teacher's profile")
    public ResponseEntity<ApiResponseDto> updateTeacherProfile(@Valid @RequestBody UpdateTeacherRequestDto request) {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User teacher = userService.getUserByCognitoUserId(username);
            
            // Verify it's a teacher
            if (teacher.getRole() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a teacher"));
            }

            // Update user fields if provided (teachers can only update their own profile)
            if (request.firstName() != null) {
                teacher.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                teacher.setLastName(request.lastName());
            }

            User updatedTeacher = userService.updateUser(teacher);
            
            // Update teacher details
            com.testpire.testpire.entity.TeacherDetails teacherDetails = teacherDetailsService.updateTeacherDetails(
                teacher,
                request.phone(),
                request.department(),
                request.subject(),
                request.qualification(),
                request.experienceYears(),
                request.specialization(),
                request.bio()
            );
            
            TeacherResponseDto response = TeacherResponseDto.fromEntity(updatedTeacher, teacherDetails);
            
            return ResponseEntity.ok(ApiResponseDto.success("Teacher profile updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating teacher profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update teacher profile: " + e.getMessage()));
        }
    }
} 