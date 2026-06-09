package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.dto.request.CreateStudentRequestDto;
import com.testpire.testpire.dto.request.StudentCriteriaDto;
import com.testpire.testpire.dto.request.StudentSearchRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.request.UpdateStudentRequestDto;
import com.testpire.testpire.dto.response.StudentResponseDto;
import com.testpire.testpire.dto.response.StudentListResponseDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.service.StudentDetailsService;
import com.testpire.testpire.service.StudentEnrollmentService;
import com.testpire.testpire.service.BatchService;
import com.testpire.testpire.dto.response.EnrollmentResponseDto;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Management", description = "Student CRUD operations and management")
public class StudentController {

    private final UserService userService;
    private final CognitoService cognitoService;
    private final InstituteService instituteService;
    private final StudentDetailsService studentDetailsService;
    private final StudentEnrollmentService studentEnrollmentService;
    private final BatchService batchService;

    // ==================== STUDENT CRUD OPERATIONS ====================

    @PostMapping
    @RequirePermission(Permission.STUDENT_CREATE)
    @Operation(summary = "Create student", description = "Create a new student (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> createStudent(@Valid @RequestBody CreateStudentRequestDto request) {
        try {
            // Tenant isolation: resolve the effective institute. For non-SUPER_ADMIN this is forced to
            // the caller's JWT institute (any body instituteId is ignored), preventing creation of users
            // in another tenant. SUPER_ADMIN may target any institute via header/body.
            Long instituteId = RequestUtils.resolveInstituteId(request.instituteId());
            if (instituteId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute ID is required"));
            }

            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute not found with ID: " + instituteId));
            }

            // Create user in Cognito (Cognito emails a temporary password to the user)
            String cognitoUserId = cognitoService.adminCreateUser(
                    request.username(), request.firstName(), request.lastName(),
                    UserRole.STUDENT, instituteId);

            // Create user in local database
            User createdUser = userService.createUser(
                    request.username(), request.firstName(), request.lastName(),
                    UserRole.STUDENT, instituteId, cognitoUserId, RequestUtils.getCurrentUsername());
            
            // Create student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.createStudentDetails(
                createdUser,
                request.phone(),
                request.course(),
                request.currentClass(),
                request.gender(),
                request.rollNumber(),
                request.parentName(),
                request.parentPhone(),
                request.parentEmail(),
                request.address(),
                request.dateOfBirth(),
                request.bloodGroup(),
                request.emergencyContact()
            );

            // Assign course+batch enrollments (if any were provided).
            if (request.enrollments() != null && !request.enrollments().isEmpty()) {
                studentEnrollmentService.syncEnrollments(
                    createdUser.getId(), instituteId, request.enrollments(), RequestUtils.getCurrentUsername());
            }
            List<EnrollmentResponseDto> enrollments = studentEnrollmentService.getEnrollments(createdUser.getId());

            StudentResponseDto response = StudentResponseDto.fromEntity(createdUser, studentDetails, enrollments);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success("Student created successfully", response)
            );
        } catch (Exception e) {
            log.error("Error creating student", e);
            return ResponseEntity.badRequest()
                .body(ApiResponseDto.error("Failed to create student: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(Permission.STUDENT_READ)
    @Operation(summary = "Get student by ID", description = "Get student details by ID")
    public ResponseEntity<ApiResponseDto> getStudentById(@PathVariable Long id) {
        try {
            com.testpire.testpire.dto.UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User student = userService.getUserById(id);

            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Institute isolation: non-SUPER_ADMIN can only access students in their own institute
            if (currentUser.role() != UserRole.SUPER_ADMIN &&
                !student.getInstituteId().equals(currentUser.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Access denied - student not in your institute"));
            }

            // Get student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.getStudentDetailsByUser(student).orElse(null);

            List<EnrollmentResponseDto> enrollments = studentEnrollmentService.getEnrollments(student.getId());
            StudentResponseDto response = StudentResponseDto.fromEntity(student, studentDetails, enrollments);
            return ResponseEntity.ok(ApiResponseDto.success("Student retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error fetching student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch student: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.STUDENT_UPDATE)
    @Operation(summary = "Update student", description = "Update student details (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequestDto request) {
        try {
            com.testpire.testpire.dto.UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User student = userService.getUserById(id);

            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Institute isolation: non-SUPER_ADMIN can only update students in their own institute
            if (currentUser.role() != UserRole.SUPER_ADMIN &&
                !student.getInstituteId().equals(currentUser.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Access denied - student not in your institute"));
            }

            // Update user fields if provided
            if (request.firstName() != null) {
                student.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                student.setLastName(request.lastName());
            }
            // Only SUPER_ADMIN may move a student to another institute; ignore body instituteId otherwise
            if (currentUser.role() == UserRole.SUPER_ADMIN && request.instituteId() != null) {
                // Validate institute exists
                if (!instituteService.instituteExistsById(request.instituteId())) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponseDto.error("Institute not found with ID: " + request.instituteId()));
                }
                student.setInstituteId(request.instituteId());
            }
            if (request.enabled() != null) {
                student.setEnabled(request.enabled());
            }

            User updatedStudent = userService.updateUser(student);
            
            // Update student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.updateStudentDetails(
                student,
                request.phone(),
                request.course(),
                request.currentClass(),
                request.gender(),
                request.rollNumber(),
                request.parentName(),
                request.parentPhone(),
                request.parentEmail(),
                request.address(),
                request.dateOfBirth(),
                request.bloodGroup(),
                request.emergencyContact()
            );
            
            // When enrollments is non-null, replace the student's full course+batch set.
            if (request.enrollments() != null) {
                studentEnrollmentService.syncEnrollments(
                    updatedStudent.getId(), updatedStudent.getInstituteId(),
                    request.enrollments(), RequestUtils.getCurrentUsername());
            }
            List<EnrollmentResponseDto> enrollments = studentEnrollmentService.getEnrollments(updatedStudent.getId());

            StudentResponseDto response = StudentResponseDto.fromEntity(updatedStudent, studentDetails, enrollments);

            return ResponseEntity.ok(ApiResponseDto.success("Student updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update student: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.STUDENT_DELETE)
    @Operation(summary = "Delete student", description = "Delete student (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> deleteStudent(@PathVariable Long id) {
        try {
            com.testpire.testpire.dto.UserDto currentUser = RequestUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User student = userService.getUserById(id);

            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Institute isolation: non-SUPER_ADMIN can only delete students in their own institute
            if (currentUser.role() != UserRole.SUPER_ADMIN &&
                !student.getInstituteId().equals(currentUser.instituteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Access denied - student not in your institute"));
            }

            // Delete student details first
            studentDetailsService.deleteStudentDetails(id);
            
            // Then delete the user
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponseDto.success("Student deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to delete student: " + e.getMessage()));
        }
    }

    // ==================== STUDENT LISTING OPERATIONS ====================

    @GetMapping
    @RequirePermission(Permission.STUDENT_LIST)
    @Operation(summary = "Get all students", description = "Get all students with optional filtering")
    public ResponseEntity<StudentListResponseDto> getAllStudents(
            @RequestParam(required = false) Long instituteId,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer currentClass) {
        try {
            // Institute isolation: non-SUPER_ADMIN may only list students in their own institute,
            // regardless of any client-supplied instituteId.
            Long callerInstituteId = RequestUtils.getCurrentUserInstituteId();
            if (callerInstituteId != null) {
                instituteId = callerInstituteId;
            }

            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList;

            if (instituteId != null && course != null) {
                // Get students by institute and course
                studentDetailsList = studentDetailsService.getStudentsByInstituteAndCourse(instituteId, course);
            } else if (instituteId != null) {
                // Get students by institute
                studentDetailsList = studentDetailsService.getStudentsByInstitute(instituteId);
            } else if (course != null) {
                // Get students by course
                studentDetailsList = studentDetailsService.getStudentsByCourse(course);
            } else {
                // Get all students
                List<User> students = userService.getUsersByRole(UserRole.STUDENT);
                studentDetailsList = students.stream()
                    .flatMap(student -> studentDetailsService.getStudentDetailsByUser(student).stream())
                    .toList();
            }

            // Apply additional filters
            List<com.testpire.testpire.entity.StudentDetails> filtered = studentDetailsList.stream()
                .filter(details -> currentClass == null || currentClass.equals(details.getCurrentClass()))
                .toList();
            List<StudentResponseDto> studentDtos = studentDetailsService.toResponsesWithEnrollments(filtered);

            StudentListResponseDto response = StudentListResponseDto.success(
                studentDtos,
                studentDtos.size(),
                0,
                studentDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StudentListResponseDto.error("Failed to fetch students: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequirePermission(Permission.STUDENT_LIST)
    @Operation(summary = "Get students by institute", description = "Get all students in a specific institute")
    public ResponseEntity<StudentListResponseDto> getStudentsByInstitute(@PathVariable Long instituteId) {
        try {
            // Institute isolation: non-SUPER_ADMIN may only list students in their own institute,
            // ignoring any instituteId supplied in the path.
            Long callerInstituteId = RequestUtils.getCurrentUserInstituteId();
            if (callerInstituteId != null) {
                instituteId = callerInstituteId;
            }

            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(StudentListResponseDto.error("Institute not found with ID: " + instituteId));
            }

            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList = studentDetailsService.getStudentsByInstitute(instituteId);
            
            List<StudentResponseDto> studentDtos = studentDetailsService.toResponsesWithEnrollments(studentDetailsList);

            StudentListResponseDto response = StudentListResponseDto.success(
                studentDtos, 
                studentDtos.size(), 
                0, 
                studentDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching students by institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StudentListResponseDto.error("Failed to fetch students: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}/course/{course}")
    @RequirePermission(Permission.STUDENT_LIST)
    @Operation(summary = "Get students by institute and course", description = "Get all students in a specific institute and course")
    public ResponseEntity<StudentListResponseDto> getStudentsByInstituteAndCourse(
            @PathVariable Long instituteId,
            @PathVariable String course) {
        try {
            // Institute isolation: non-SUPER_ADMIN may only list students in their own institute,
            // ignoring any instituteId supplied in the path.
            Long callerInstituteId = RequestUtils.getCurrentUserInstituteId();
            if (callerInstituteId != null) {
                instituteId = callerInstituteId;
            }

            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(StudentListResponseDto.error("Institute not found with ID: " + instituteId));
            }

            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList = studentDetailsService.getStudentsByInstituteAndCourse(instituteId, course);
            
            List<StudentResponseDto> studentDtos = studentDetailsService.toResponsesWithEnrollments(studentDetailsList);

            StudentListResponseDto response = StudentListResponseDto.success(
                studentDtos, 
                studentDtos.size(), 
                0, 
                studentDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching students by institute and course", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StudentListResponseDto.error("Failed to fetch students: " + e.getMessage()));
        }
    }

    @GetMapping("/batch/{batchId}")
    @RequirePermission(Permission.STUDENT_LIST)
    @Operation(summary = "Get students in a batch", description = "List all students enrolled in a given batch")
    public ResponseEntity<StudentListResponseDto> getStudentsByBatch(@PathVariable Long batchId) {
        try {
            // Tenancy: resolves the batch scoped to the caller's institute (throws if not visible),
            // so a non-SUPER_ADMIN cannot list students of another tenant's batch.
            batchService.getBatchById(batchId);

            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList =
                studentDetailsService.getStudentsByBatch(batchId);

            List<StudentResponseDto> studentDtos = studentDetailsService.toResponsesWithEnrollments(studentDetailsList);

            return ResponseEntity.ok(StudentListResponseDto.success(
                studentDtos, studentDtos.size(), 0, studentDtos.size()));
        } catch (Exception e) {
            log.error("Error fetching students by batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StudentListResponseDto.error("Failed to fetch students: " + e.getMessage()));
        }
    }

    // ==================== ADVANCED SEARCH OPERATIONS ====================

    @PostMapping("/search/advanced")
    @RequirePermission(Permission.STUDENT_SEARCH)
    @Operation(summary = "Advanced search students", description = "Search students with advanced criteria, pagination, and sorting")
    public ResponseEntity<ApiResponseDto> searchStudentsAdvanced(@Valid @RequestBody StudentSearchRequestDto request) {
        try {
            // Non-SUPER_ADMIN: scoped to their JWT institute. SUPER_ADMIN: honor the
            // acting-institute header / criteria.instituteId via resolveInstituteId.
            Long instituteId = RequestUtils.resolveInstituteId(request.getInstituteId());
            log.info("Advanced search for students with criteria: {}", request);
            // Enforce tenant scope: overwrite whatever instituteId the client sent.
            StudentCriteriaDto criteria = request.getCriteria() != null ? request.getCriteria() : new StudentCriteriaDto();
            criteria.setInstituteId(instituteId);
            request.setCriteria(criteria);
            StudentListResponseDto students = studentDetailsService.searchStudentsWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Students retrieved successfully", students));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search students: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequirePermission(Permission.STUDENT_SEARCH)
    @Operation(summary = "Advanced search students (GET)", description = "Search students with advanced criteria via GET parameters")
    public ResponseEntity<ApiResponseDto> searchStudentsAdvancedGet(
            @Parameter(description = "Search text (optional)", example = "john")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "First name (optional)", example = "John")
            @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name (optional)", example = "Doe")
            @RequestParam(required = false) String lastName,
            @Parameter(description = "Username (optional)", example = "johndoe")
            @RequestParam(required = false) String username,
            @Parameter(description = "Email (optional)", example = "john@example.com")
            @RequestParam(required = false) String email,
            @Parameter(description = "Phone (optional)", example = "1234567890")
            @RequestParam(required = false) String phone,
            @Parameter(description = "Course name, legacy free-text (optional)", example = "Computer Science")
            @RequestParam(required = false) String course,
            @Parameter(description = "Enrolled course ID — filters by student_enrollments (optional)", example = "5")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "Enrolled batch ID — filters by student_enrollments (optional)", example = "12")
            @RequestParam(required = false) Long batchId,
            @Parameter(description = "Minimum class (optional)", example = "1")
            @RequestParam(required = false) Integer minCurrentClass,
            @Parameter(description = "Maximum class (optional)", example = "14")
            @RequestParam(required = false) Integer maxCurrentClass,
            @Parameter(description = "Roll number (optional)", example = "CS2024001")
            @RequestParam(required = false) String rollNumber,
            @Parameter(description = "Parent name (optional)", example = "Jane Doe")
            @RequestParam(required = false) String parentName,
            @Parameter(description = "Parent phone (optional)", example = "9876543210")
            @RequestParam(required = false) String parentPhone,
            @Parameter(description = "Parent email (optional)", example = "jane@example.com")
            @RequestParam(required = false) String parentEmail,
            @Parameter(description = "Address (optional)", example = "123 Main St")
            @RequestParam(required = false) String address,
            @Parameter(description = "Blood group (optional)", example = "O+")
            @RequestParam(required = false) String bloodGroup,
            @Parameter(description = "Emergency contact (optional)", example = "9876543210")
            @RequestParam(required = false) String emergencyContact,
            @Parameter(description = "Enabled status (optional)", example = "true")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Created after (optional)", example = "2024-01-01T00:00:00")
            @RequestParam(required = false) String createdAfter,
            @Parameter(description = "Created before (optional)", example = "2024-12-31T23:59:59")
            @RequestParam(required = false) String createdBefore,
            @Parameter(description = "Created by (optional)", example = "admin")
            @RequestParam(required = false) String createdBy,
            @Parameter(description = "Page number (optional)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size (optional)", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Sort by field (optional)", example = "createdAt")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (optional)", example = "desc")
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        try {
            // No instituteId GET param; non-SA scoped to JWT, SA honors X-Institute-Id header.
            Long instituteId = RequestUtils.resolveInstituteId(null);
            log.info("Advanced search for students with GET parameters");
            
            // Parse date parameters
            Instant parsedCreatedAfter = null;
            if (createdAfter != null && !createdAfter.isEmpty()) {
                try {
                    parsedCreatedAfter = LocalDateTime.parse(createdAfter, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC);
                } catch (Exception e) {
                    log.warn("Invalid createdAfter date format: {}", createdAfter);
                }
            }
            
            Instant parsedCreatedBefore = null;
            if (createdBefore != null && !createdBefore.isEmpty()) {
                try {
                    parsedCreatedBefore = LocalDateTime.parse(createdBefore, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC);
                } catch (Exception e) {
                    log.warn("Invalid createdBefore date format: {}", createdBefore);
                }
            }
            
            StudentCriteriaDto criteria = StudentCriteriaDto.builder()
                    .instituteId(instituteId)
                    .searchText(searchText)
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(username)
                    .email(email)
                    .phone(phone)
                    .course(course)
                    .courseId(courseId)
                    .batchId(batchId)
                    .minCurrentClass(minCurrentClass)
                    .maxCurrentClass(maxCurrentClass)
                    .rollNumber(rollNumber)
                    .parentName(parentName)
                    .parentPhone(parentPhone)
                    .parentEmail(parentEmail)
                    .address(address)
                    .bloodGroup(bloodGroup)
                    .emergencyContact(emergencyContact)
                    .enabled(enabled)
                    .createdAfter(parsedCreatedAfter)
                    .createdBefore(parsedCreatedBefore)
                    .createdBy(createdBy)
                    .build();
                    
            PaginationRequestDto pagination = PaginationRequestDto.builder()
                    .page(page)
                    .size(size)
                    .build();
                    
            SortingRequestDto sorting = SortingRequestDto.builder()
                    .field(sortBy)
                    .direction(sortDirection)
                    .build();
                    
            StudentSearchRequestDto request = StudentSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            StudentListResponseDto students = studentDetailsService.searchStudentsWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Students retrieved successfully", students));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search students: " + e.getMessage()));
        }
    }

    // ==================== STUDENT PROFILE OPERATIONS ====================

    @GetMapping("/profile")
    @RequirePermission(Permission.STUDENT_PROFILE_READ)
    @Operation(summary = "Get student profile", description = "Get current student's profile")
    public ResponseEntity<ApiResponseDto> getStudentProfile() {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User student = userService.getUserByCognitoUserId(username);
            
            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Get student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.getStudentDetailsByUser(student).orElse(null);
            
            List<EnrollmentResponseDto> enrollments = studentEnrollmentService.getEnrollments(student.getId());
            StudentResponseDto response = StudentResponseDto.fromEntity(student, studentDetails, enrollments);
            return ResponseEntity.ok(ApiResponseDto.success("Student profile retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error fetching student profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch student profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @RequirePermission(Permission.STUDENT_PROFILE_UPDATE)
    @Operation(summary = "Update student profile", description = "Update current student's profile")
    public ResponseEntity<ApiResponseDto> updateStudentProfile(@Valid @RequestBody UpdateStudentRequestDto request) {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("User not found"));
            }

            User student = userService.getUserByCognitoUserId(username);
            
            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Update user fields if provided (students can only update their own profile)
            if (request.firstName() != null) {
                student.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                student.setLastName(request.lastName());
            }

            User updatedStudent = userService.updateUser(student);
            
            // Update student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.updateStudentDetails(
                student,
                request.phone(),
                request.course(),
                request.currentClass(),
                request.gender(),
                request.rollNumber(),
                request.parentName(),
                request.parentPhone(),
                request.parentEmail(),
                request.address(),
                request.dateOfBirth(),
                request.bloodGroup(),
                request.emergencyContact()
            );
            
            // Enrollment assignment is an admin action (STUDENT_UPDATE), not part of self-service
            // profile update — request.enrollments() is intentionally ignored here.
            List<EnrollmentResponseDto> enrollments = studentEnrollmentService.getEnrollments(updatedStudent.getId());
            StudentResponseDto response = StudentResponseDto.fromEntity(updatedStudent, studentDetails, enrollments);

            return ResponseEntity.ok(ApiResponseDto.success("Student profile updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating student profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update student profile: " + e.getMessage()));
        }
    }

    // ==================== LEGACY ENDPOINTS (for backward compatibility) ====================

    @GetMapping("/peers")
    @RequirePermission(Permission.STUDENT_PEERS_READ)
    @Operation(summary = "Get student peers", description = "Get all students in the same institute")
    public ResponseEntity<StudentListResponseDto> getPeers() {
        try {
            String username = RequestUtils.getCurrentUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(StudentListResponseDto.error("User not found"));
            }

            User student = userService.getUserByCognitoUserId(username);
            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList = studentDetailsService.getStudentsByInstitute(student.getInstituteId());

            List<StudentResponseDto> studentDtos = studentDetailsList.stream()
                .map(details -> StudentResponseDto.peerView(details.getUser(), details))
                .toList();

            StudentListResponseDto response = StudentListResponseDto.success(
                studentDtos, 
                studentDtos.size(), 
                0, 
                studentDtos.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching student peers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StudentListResponseDto.error("Failed to fetch peers: " + e.getMessage()));
        }
    }

}
