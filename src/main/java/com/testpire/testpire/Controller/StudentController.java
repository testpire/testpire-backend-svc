package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.RegisterRequest;
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

import java.time.LocalDateTime;
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

    // ==================== STUDENT CRUD OPERATIONS ====================

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Create student", description = "Create a new student (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> createStudent(@Valid @RequestBody CreateStudentRequestDto request) {
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
                UserRole.STUDENT,
                request.instituteId()
            );

            // Create user in Cognito
            String cognitoUserId = cognitoService.signUp(registerRequest, UserRole.STUDENT);
            
            // Create user in local database
            User createdUser = userService.createUser(registerRequest, UserRole.STUDENT, cognitoUserId, RequestUtils.getCurrentUsername());
            
            // Create student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.createStudentDetails(
                createdUser,
                request.phone(),
                request.course(),
                request.yearOfStudy(),
                request.rollNumber(),
                request.parentName(),
                request.parentPhone(),
                request.parentEmail(),
                request.address(),
                request.dateOfBirth(),
                request.bloodGroup(),
                request.emergencyContact()
            );

            StudentResponseDto response = StudentResponseDto.fromEntity(createdUser, studentDetails);
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
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(summary = "Get student by ID", description = "Get student details by ID")
    public ResponseEntity<ApiResponseDto> getStudentById(@PathVariable Long id) {
        try {
            User student = userService.getUserById(id);
            
            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Get student details
            com.testpire.testpire.entity.StudentDetails studentDetails = studentDetailsService.getStudentDetailsByUser(student).orElse(null);
            
            StudentResponseDto response = StudentResponseDto.fromEntity(student, studentDetails);
            return ResponseEntity.ok(ApiResponseDto.success("Student retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error fetching student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch student: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Update student", description = "Update student details (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequestDto request) {
        try {
            User student = userService.getUserById(id);
            
            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
            }

            // Update user fields if provided
            if (request.firstName() != null) {
                student.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                student.setLastName(request.lastName());
            }
            if (request.instituteId() != null) {
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
                request.yearOfStudy(),
                request.rollNumber(),
                request.parentName(),
                request.parentPhone(),
                request.parentEmail(),
                request.address(),
                request.dateOfBirth(),
                request.bloodGroup(),
                request.emergencyContact()
            );
            
            StudentResponseDto response = StudentResponseDto.fromEntity(updatedStudent, studentDetails);
            
            return ResponseEntity.ok(ApiResponseDto.success("Student updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating student", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update student: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Delete student", description = "Delete student (SUPER_ADMIN or INST_ADMIN)")
    public ResponseEntity<ApiResponseDto> deleteStudent(@PathVariable Long id) {
        try {
            User student = userService.getUserById(id);
            
            // Verify it's a student
            if (student.getRole() != UserRole.STUDENT) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("User is not a student"));
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
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get all students", description = "Get all students with optional filtering")
    public ResponseEntity<StudentListResponseDto> getAllStudents(
            @RequestParam(required = false) Long instituteId,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer yearOfStudy) {
        try {
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
                    .map(student -> studentDetailsService.getStudentDetailsByUser(student).orElse(null))
                    .filter(details -> details != null)
                    .toList();
            }

            // Apply additional filters
            List<StudentResponseDto> studentDtos = studentDetailsList.stream()
                .filter(details -> yearOfStudy == null || yearOfStudy.equals(details.getYearOfStudy()))
                .map(details -> StudentResponseDto.fromEntity(details.getUser(), details))
                .toList();

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
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get students by institute", description = "Get all students in a specific institute")
    public ResponseEntity<StudentListResponseDto> getStudentsByInstitute(@PathVariable Long instituteId) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(StudentListResponseDto.error("Institute not found with ID: " + instituteId));
            }

            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList = studentDetailsService.getStudentsByInstitute(instituteId);
            
            List<StudentResponseDto> studentDtos = studentDetailsList.stream()
                .map(details -> StudentResponseDto.fromEntity(details.getUser(), details))
                .toList();

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
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get students by institute and course", description = "Get all students in a specific institute and course")
    public ResponseEntity<StudentListResponseDto> getStudentsByInstituteAndCourse(
            @PathVariable Long instituteId, 
            @PathVariable String course) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(StudentListResponseDto.error("Institute not found with ID: " + instituteId));
            }

            List<com.testpire.testpire.entity.StudentDetails> studentDetailsList = studentDetailsService.getStudentsByInstituteAndCourse(instituteId, course);
            
            List<StudentResponseDto> studentDtos = studentDetailsList.stream()
                .map(details -> StudentResponseDto.fromEntity(details.getUser(), details))
                .toList();

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

    // ==================== ADVANCED SEARCH OPERATIONS ====================

    @PostMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(summary = "Advanced search students", description = "Search students with advanced criteria, pagination, and sorting")
    public ResponseEntity<ApiResponseDto> searchStudentsAdvanced(@Valid @RequestBody StudentSearchRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for students with criteria: {}", request);
            
            // Set instituteId from JWT token
            StudentCriteriaDto criteria = StudentCriteriaDto.builder()
                    .instituteId(instituteId)
                    .searchText(request.getSearchText())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .course(request.getCourse())
                    .minYearOfStudy(request.getMinYearOfStudy())
                    .maxYearOfStudy(request.getMaxYearOfStudy())
                    .rollNumber(request.getRollNumber())
                    .parentName(request.getParentName())
                    .parentPhone(request.getParentPhone())
                    .parentEmail(request.getParentEmail())
                    .address(request.getAddress())
                    .bloodGroup(request.getBloodGroup())
                    .emergencyContact(request.getEmergencyContact())
                    .enabled(request.getEnabled())
                    .createdAfter(request.getCreatedAfter())
                    .createdBefore(request.getCreatedBefore())
                    .createdBy(request.getCreatedBy())
                    .build();
                    
            StudentSearchRequestDto requestWithInstituteId = StudentSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(request.getPagination())
                    .sorting(request.getSorting())
                    .build();
                    
            StudentListResponseDto students = studentDetailsService.searchStudentsWithSpecification(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Students retrieved successfully", students));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search students: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
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
            @Parameter(description = "Course (optional)", example = "Computer Science")
            @RequestParam(required = false) String course,
            @Parameter(description = "Minimum year of study (optional)", example = "1")
            @RequestParam(required = false) Integer minYearOfStudy,
            @Parameter(description = "Maximum year of study (optional)", example = "4")
            @RequestParam(required = false) Integer maxYearOfStudy,
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
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for students with GET parameters");
            
            // Parse date parameters
            LocalDateTime parsedCreatedAfter = null;
            if (createdAfter != null && !createdAfter.isEmpty()) {
                try {
                    parsedCreatedAfter = LocalDateTime.parse(createdAfter, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    log.warn("Invalid createdAfter date format: {}", createdAfter);
                }
            }
            
            LocalDateTime parsedCreatedBefore = null;
            if (createdBefore != null && !createdBefore.isEmpty()) {
                try {
                    parsedCreatedBefore = LocalDateTime.parse(createdBefore, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
                    .minYearOfStudy(minYearOfStudy)
                    .maxYearOfStudy(maxYearOfStudy)
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
    @RequireRole(UserRole.STUDENT)
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
            
            StudentResponseDto response = StudentResponseDto.fromEntity(student, studentDetails);
            return ResponseEntity.ok(ApiResponseDto.success("Student profile retrieved successfully", response));
        } catch (Exception e) {
            log.error("Error fetching student profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch student profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @RequireRole(UserRole.STUDENT)
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
                request.yearOfStudy(),
                request.rollNumber(),
                request.parentName(),
                request.parentPhone(),
                request.parentEmail(),
                request.address(),
                request.dateOfBirth(),
                request.bloodGroup(),
                request.emergencyContact()
            );
            
            StudentResponseDto response = StudentResponseDto.fromEntity(updatedStudent, studentDetails);
            
            return ResponseEntity.ok(ApiResponseDto.success("Student profile updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating student profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update student profile: " + e.getMessage()));
        }
    }

    // ==================== LEGACY ENDPOINTS (for backward compatibility) ====================

    @GetMapping("/peers")
    @RequireRole(UserRole.STUDENT)
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
                .map(details -> StudentResponseDto.fromEntity(details.getUser(), details))
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

    // ==================== DEBUG ENDPOINTS ====================

    @GetMapping("/debug")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Debug students", description = "Debug endpoint to check student data")
    public ResponseEntity<ApiResponseDto> debugStudents() {
        try {
            studentDetailsService.debugStudentCount();
            return ResponseEntity.ok(ApiResponseDto.success("Debug information logged"));
        } catch (Exception e) {
            log.error("Error in debug", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Debug failed: " + e.getMessage()));
        }
    }
} 