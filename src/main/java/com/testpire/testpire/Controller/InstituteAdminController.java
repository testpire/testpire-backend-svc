package com.testpire.testpire.Controller;

import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inst-admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Institute Admin", description = "Institute admin operations - INST_ADMIN and SUPER_ADMIN")
public class InstituteAdminController {

    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // ========== TEACHER MANAGEMENT ==========

    @PostMapping("/register/teacher")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN')")
    @Operation(summary = "Register teacher", description = "Register a new teacher (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<?> registerTeacher(@Valid @RequestBody RegisterRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            // Validate that the user is trying to create a teacher
            if (request.role() != UserRole.TEACHER) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating teachers"));
            }

            // Get current user's institute ID for validation
            String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // INST_ADMIN can only create teachers in their own institute
            if (currentUser.getRole() == UserRole.INST_ADMIN && 
                !currentUser.getInstituteId().equals(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Institute admins can only create teachers in their own institute"));
            }

            String cognitoUserId = cognitoService.signUp(request, UserRole.TEACHER);
            User createdUser = userService.createUser(request, UserRole.TEACHER, cognitoUserId, currentUsername);

            return ResponseEntity.ok(Map.of(
                "userId", createdUser.getId(),
                "cognitoUserId", cognitoUserId,
                "message", "Teacher registered successfully"
            ));
        } catch (Exception e) {
            log.error("Teacher registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Teacher registration failed", "message", e.getMessage()));
        }
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN')")
    @Operation(summary = "Get teachers", description = "Get all teachers in the institute (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<?> getTeachers(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            List<User> teachers = userService.getUsersByRoleAndInstitute(UserRole.TEACHER, currentUser.getInstituteId());
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error fetching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch teachers"));
        }
    }

    @GetMapping("/teachers/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN')")
    @Operation(summary = "Search teachers", description = "Search teachers in the institute")
    public ResponseEntity<?> searchTeachers(@RequestParam String searchTerm, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            List<User> teachers = userService.searchUsersByRoleAndInstitute(
                UserRole.TEACHER, currentUser.getInstituteId(), searchTerm);
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            log.error("Error searching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search teachers"));
        }
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN')")
    @Operation(summary = "Get teacher by ID", description = "Get teacher details by ID")
    public ResponseEntity<?> getTeacherById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            User teacher = userService.getUserById(id);
            
            // Verify the teacher belongs to the same institute
            if (!teacher.getInstituteId().equals(currentUser.getInstituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - teacher not in your institute"));
            }

            return ResponseEntity.ok(teacher);
        } catch (Exception e) {
            log.error("Error fetching teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch teacher"));
        }
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN')")
    @Operation(summary = "Update teacher", description = "Update teacher details")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, 
                                        @Valid @RequestBody RegisterRequest request,
                                        HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            User existingTeacher = userService.getUserById(id);
            
            // Verify the teacher belongs to the same institute
            if (!existingTeacher.getInstituteId().equals(currentUser.getInstituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - teacher not in your institute"));
            }

            User updatedTeacher = userService.updateUser(id, request, currentUsername);
            return ResponseEntity.ok(Map.of(
                "message", "Teacher updated successfully",
                "teacherId", updatedTeacher.getId()
            ));
        } catch (Exception e) {
            log.error("Error updating teacher", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update teacher", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN')")
    @Operation(summary = "Delete teacher", description = "Deactivate a teacher")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            User existingTeacher = userService.getUserById(id);
            
            // Verify the teacher belongs to the same institute
            if (!existingTeacher.getInstituteId().equals(currentUser.getInstituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - teacher not in your institute"));
            }

            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Teacher deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deleting teacher", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to delete teacher", "message", e.getMessage()));
        }
    }

    // ========== STUDENT MANAGEMENT ==========

    @PostMapping("/register/student")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER')")
    @Operation(summary = "Register student", description = "Register a new student (SUPER_ADMIN, INST_ADMIN, or TEACHER)")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody RegisterRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            // Validate that the user is trying to create a student
            if (request.role() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "This endpoint is only for creating students"));
            }

            // Get current user's institute ID for validation
            String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            // INST_ADMIN and TEACHER can only create students in their own institute
            if ((currentUser.getRole() == UserRole.INST_ADMIN || currentUser.getRole() == UserRole.TEACHER) && 
                !currentUser.getInstituteId().equals(request.instituteId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "You can only create students in your own institute"));
            }

            String cognitoUserId = cognitoService.signUp(request, UserRole.STUDENT);
            User createdUser = userService.createUser(request, UserRole.STUDENT, cognitoUserId, currentUsername);

            return ResponseEntity.ok(Map.of(
                "userId", createdUser.getId(),
                "cognitoUserId", cognitoUserId,
                "message", "Student registered successfully"
            ));
        } catch (Exception e) {
            log.error("Student registration failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Student registration failed", "message", e.getMessage()));
        }
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER')")
    @Operation(summary = "Get students", description = "Get all students in the institute (SUPER_ADMIN, INST_ADMIN, or TEACHER)")
    public ResponseEntity<?> getStudents(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            List<User> students = userService.getUsersByRoleAndInstitute(UserRole.STUDENT, currentUser.getInstituteId());
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch students"));
        }
    }

    @GetMapping("/students/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER')")
    @Operation(summary = "Search students", description = "Search students in the institute")
    public ResponseEntity<?> searchStudents(@RequestParam String searchTerm, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            List<User> students = userService.searchUsersByRoleAndInstitute(
                UserRole.STUDENT, currentUser.getInstituteId(), searchTerm);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error searching students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search students"));
        }
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER')")
    @Operation(summary = "Get student by ID", description = "Get student details by ID")
    public ResponseEntity<?> getStudentById(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            User student = userService.getUserById(id);
            
            // Verify the student belongs to the same institute
            if (!student.getInstituteId().equals(currentUser.getInstituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - student not in your institute"));
            }

            return ResponseEntity.ok(student);
        } catch (Exception e) {
            log.error("Error fetching student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch student"));
        }
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER')")
    @Operation(summary = "Update student", description = "Update student details")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, 
                                        @Valid @RequestBody RegisterRequest request,
                                        HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            User existingStudent = userService.getUserById(id);
            
            // Verify the student belongs to the same institute
            if (!existingStudent.getInstituteId().equals(currentUser.getInstituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - student not in your institute"));
            }

            User updatedStudent = userService.updateUser(id, request, currentUsername);
            return ResponseEntity.ok(Map.of(
                "message", "Student updated successfully",
                "studentId", updatedStudent.getId()
            ));
        } catch (Exception e) {
            log.error("Error updating student", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update student", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'INST_ADMIN', 'TEACHER')")
    @Operation(summary = "Delete student", description = "Deactivate a student")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String currentUsername = jwtUtil.extractUsername(token);
            User currentUser = userService.getUserByUsername(currentUsername);

            User existingStudent = userService.getUserById(id);
            
            // Verify the student belongs to the same institute
            if (!existingStudent.getInstituteId().equals(currentUser.getInstituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied - student not in your institute"));
            }

            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Student deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deleting student", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to delete student", "message", e.getMessage()));
        }
    }
} 