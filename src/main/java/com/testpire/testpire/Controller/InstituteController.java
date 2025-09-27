package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.InstituteDto;
import com.testpire.testpire.dto.RegisterRequest;
import com.testpire.testpire.dto.request.CreateInstituteRequestDto;
import com.testpire.testpire.dto.request.UpdateInstituteRequestDto;
import com.testpire.testpire.dto.request.CreateUserRequestDto;
import com.testpire.testpire.dto.request.InstituteSearchRequestDto;
import com.testpire.testpire.dto.request.InstituteCriteriaDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.response.InstituteResponseDto;
import com.testpire.testpire.dto.response.InstituteListResponseDto;
import com.testpire.testpire.dto.response.UserListResponseDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/institutes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Institute Management", description = "Institute and user management operations")
public class InstituteController {

    private final InstituteService instituteService;
    private final CognitoService cognitoService;
    private final UserService userService;

    // ==================== INSTITUTE CRUD OPERATIONS ====================

    @PostMapping
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Create institute", description = "Create a new institute (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponseDto> createInstitute(@Valid @RequestBody CreateInstituteRequestDto request) {
        try {
            String username = RequestUtils.getCurrentUsername();
            
            // Convert DTO to entity
            InstituteDto instituteDto = new InstituteDto(
                request.name(),
                request.code(),
                request.address() != null ? request.address() : "",
                request.city() != null ? request.city() : "",
                request.state() != null ? request.state() : "",
                request.country() != null ? request.country() : "",
                request.postalCode() != null ? request.postalCode() : "",
                request.phone() != null ? request.phone() : "",
                request.email() != null ? request.email() : "",
                request.website(),
                request.description()
            );
            
            Institute createdInstitute = instituteService.createInstitute(instituteDto, username);
            InstituteResponseDto response = InstituteResponseDto.fromEntity(createdInstitute);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success("Institute created successfully", response)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to create institute: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Update institute", description = "Update an existing institute (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponseDto> updateInstitute(@PathVariable Long id, @Valid @RequestBody UpdateInstituteRequestDto request) {
        try {
            String username = RequestUtils.getCurrentUsername();
            
            // Get existing institute to preserve required fields
            Institute existingInstitute = instituteService.getInstituteById(id);
            
            // Convert DTO to entity
            InstituteDto instituteDto = new InstituteDto(
                request.name() != null ? request.name() : existingInstitute.getName(),
                request.code() != null ? request.code() : existingInstitute.getCode(),
                request.address() != null ? request.address() : existingInstitute.getAddress(),
                request.city() != null ? request.city() : existingInstitute.getCity(),
                request.state() != null ? request.state() : existingInstitute.getState(),
                request.country() != null ? request.country() : existingInstitute.getCountry(),
                request.postalCode() != null ? request.postalCode() : existingInstitute.getPostalCode(),
                request.phone() != null ? request.phone() : existingInstitute.getPhone(),
                request.email() != null ? request.email() : existingInstitute.getEmail(),
                request.website() != null ? request.website() : existingInstitute.getWebsite(),
                request.description() != null ? request.description() : existingInstitute.getDescription()
            );
            
            Institute updatedInstitute = instituteService.updateInstitute(id, instituteDto, username);
            InstituteResponseDto response = InstituteResponseDto.fromEntity(updatedInstitute);
            
            return ResponseEntity.ok(ApiResponseDto.success("Institute updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update institute: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Delete institute", description = "Delete an institute (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponseDto> deleteInstitute(@PathVariable Long id) {
        try {
            instituteService.deleteInstitute(id);
            return ResponseEntity.ok(ApiResponseDto.success("Institute deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to delete institute: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Get institute by ID", description = "Get institute details by ID (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponseDto> getInstituteById(@PathVariable Long id) {
        try {
            Institute institute = instituteService.getInstituteById(id);
            InstituteResponseDto response = InstituteResponseDto.fromEntity(institute);
            return ResponseEntity.ok(ApiResponseDto.success("Institute retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch institute: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get institute by code", description = "Get institute details by code (public endpoint)")
    public ResponseEntity<ApiResponseDto> getInstituteByCode(@PathVariable String code) {
        try {
            Institute institute = instituteService.getInstituteByCode(code);
            InstituteResponseDto response = InstituteResponseDto.fromEntity(institute);
            return ResponseEntity.ok(ApiResponseDto.success("Institute retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching institute by code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch institute: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Get all institutes", description = "Get all active institutes (SUPER_ADMIN only)")
    public ResponseEntity<InstituteListResponseDto> getAllInstitutes() {
        try {
            List<Institute> institutes = instituteService.getAllActiveInstitutes();
            
            List<InstituteResponseDto> instituteDtos = institutes.stream()
                .map(InstituteResponseDto::fromEntity)
                .toList();
            
            InstituteListResponseDto response = InstituteListResponseDto.success(
                instituteDtos, 
                institutes.size(), 
                0, 
                institutes.size()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching institutes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(InstituteListResponseDto.error("Failed to fetch institutes: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole(UserRole.SUPER_ADMIN)
    @Operation(summary = "Search institutes", description = "Search institutes by name or code (SUPER_ADMIN only)")
    public ResponseEntity<InstituteListResponseDto> searchInstitutes(@RequestParam String query) {
        try {
            List<Institute> institutes = instituteService.searchInstitutes(query);
            
            List<InstituteResponseDto> instituteDtos = institutes.stream()
                .map(InstituteResponseDto::fromEntity)
                .toList();
            
            InstituteListResponseDto response = InstituteListResponseDto.success(
                instituteDtos, 
                institutes.size(), 
                0, 
                institutes.size()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching institutes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(InstituteListResponseDto.error("Failed to search institutes: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequireRole(UserRole.INST_ADMIN)
    @Operation(summary = "Advanced search institutes", description = "Search institutes with advanced criteria, pagination, and sorting (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponseDto> searchInstitutesAdvanced(@Valid @RequestBody InstituteSearchRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for institutes with instId: {}, criteria: {}", instituteId, request);
            request.getCriteria().setInstituteId(instituteId);
            InstituteListResponseDto institutes = instituteService.searchInstitutesWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Institutes retrieved successfully", institutes));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search institutes: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequireRole(UserRole.INST_ADMIN)
    @Operation(summary = "Advanced search institutes (GET)", description = "Search institutes with advanced criteria via GET parameters (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponseDto> searchInstitutesAdvancedGet(
            @Parameter(description = "Search text for name, code, address, city, state, country, email, website, or description")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Institute name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Institute code")
            @RequestParam(required = false) String code,
            @Parameter(description = "Address")
            @RequestParam(required = false) String address,
            @Parameter(description = "City")
            @RequestParam(required = false) String city,
            @Parameter(description = "State")
            @RequestParam(required = false) String state,
            @Parameter(description = "Country")
            @RequestParam(required = false) String country,
            @Parameter(description = "Postal code")
            @RequestParam(required = false) String postalCode,
            @Parameter(description = "Phone number")
            @RequestParam(required = false) String phone,
            @Parameter(description = "Email address")
            @RequestParam(required = false) String email,
            @Parameter(description = "Website URL")
            @RequestParam(required = false) String website,
            @Parameter(description = "Description")
            @RequestParam(required = false) String description,
            @Parameter(description = "Active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Created after date (yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) String createdAfter,
            @Parameter(description = "Created before date (yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) String createdBefore,
            @Parameter(description = "Created by username")
            @RequestParam(required = false) String createdBy,
            @Parameter(description = "Updated by username")
            @RequestParam(required = false) String updatedBy,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            log.info("Advanced search for institutes with GET parameters");
            
            // Parse date parameters
            LocalDateTime parsedCreatedAfter = null;
            if (createdAfter != null && !createdAfter.isEmpty()) {
                try {
                    parsedCreatedAfter = LocalDateTime.parse(createdAfter, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    log.warn("Invalid createdAfter date format: {}", createdAfter);
                }
            }
            
            LocalDateTime parsedCreatedBefore = null;
            if (createdBefore != null && !createdBefore.isEmpty()) {
                try {
                    parsedCreatedBefore = LocalDateTime.parse(createdBefore, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    log.warn("Invalid createdBefore date format: {}", createdBefore);
                }
            }
            
            InstituteCriteriaDto criteria = InstituteCriteriaDto.builder()
                    .searchText(searchText)
                    .name(name)
                    .code(code)
                    .address(address)
                    .city(city)
                    .state(state)
                    .country(country)
                    .postalCode(postalCode)
                    .phone(phone)
                    .email(email)
                    .website(website)
                    .description(description)
                    .active(active)
                    .createdAfter(parsedCreatedAfter)
                    .createdBefore(parsedCreatedBefore)
                    .createdBy(createdBy)
                    .updatedBy(updatedBy)
                    .build();
            
            PaginationRequestDto pagination = PaginationRequestDto.builder()
                    .page(page)
                    .size(size)
                    .build();
            
            SortingRequestDto sorting = SortingRequestDto.builder()
                    .field(sortBy)
                    .direction(sortDirection)
                    .build();
            
            InstituteSearchRequestDto request = InstituteSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            InstituteListResponseDto institutes = instituteService.searchInstitutesWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Institutes retrieved successfully", institutes));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search institutes: " + e.getMessage()));
        }
    }

    // ==================== USER MANAGEMENT WITHIN INSTITUTES ====================

    @PostMapping("/{instituteId}/users/teachers")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Register teacher", description = "Register a new teacher in the institute")
    public ResponseEntity<ApiResponseDto> registerTeacher(@PathVariable Long instituteId, @Valid @RequestBody CreateUserRequestDto request) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute not found with ID: " + instituteId));
            }

            // Ensure the user is being created for the correct institute
            CreateUserRequestDto teacherRequest = new CreateUserRequestDto(
                request.username(),
                request.password(),
                request.firstName(),
                request.lastName(),
                UserRole.TEACHER,
                instituteId
            );

            // Convert to RegisterRequest for Cognito
            RegisterRequest registerRequest = new RegisterRequest(
                teacherRequest.username(),
                teacherRequest.username(), // email same as username
                teacherRequest.password(),
                teacherRequest.firstName(),
                teacherRequest.lastName(),
                UserRole.TEACHER,
                instituteId
            );

            String cognitoUserId = cognitoService.signUp(registerRequest, UserRole.TEACHER);
            User createdUser = userService.createUser(registerRequest, UserRole.TEACHER, cognitoUserId, RequestUtils.getCurrentUsername());

            return ResponseEntity.ok(ApiResponseDto.success(
                "Teacher registered successfully",
                Map.of(
                    "userId", createdUser.getId(),
                    "cognitoUserId", cognitoUserId,
                    "instituteId", instituteId
                )
            ));
        } catch (Exception e) {
            log.error("Error registering teacher", e);
            return ResponseEntity.badRequest()
                .body(ApiResponseDto.error("Failed to register teacher: " + e.getMessage()));
        }
    }

    @PostMapping("/{instituteId}/users/students")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Register student", description = "Register a new student in the institute")
    public ResponseEntity<ApiResponseDto> registerStudent(@PathVariable Long instituteId, @Valid @RequestBody CreateUserRequestDto request) {
        try {
            // Validate institute exists
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute not found with ID: " + instituteId));
            }

            // Ensure the user is being created for the correct institute
            CreateUserRequestDto studentRequest = new CreateUserRequestDto(
                request.username(),
                request.password(),
                request.firstName(),
                request.lastName(),
                UserRole.STUDENT,
                instituteId
            );

            // Convert to RegisterRequest for Cognito
            RegisterRequest registerRequest = new RegisterRequest(
                studentRequest.username(),
                studentRequest.username(), // email same as username
                studentRequest.password(),
                studentRequest.firstName(),
                studentRequest.lastName(),
                UserRole.STUDENT,
                instituteId
            );

            String cognitoUserId = cognitoService.signUp(registerRequest, UserRole.STUDENT);
            User createdUser = userService.createUser(registerRequest, UserRole.STUDENT, cognitoUserId, RequestUtils.getCurrentUsername());

            return ResponseEntity.ok(ApiResponseDto.success(
                "Student registered successfully",
                Map.of(
                    "userId", createdUser.getId(),
                    "cognitoUserId", cognitoUserId,
                    "instituteId", instituteId
                )
            ));
        } catch (Exception e) {
            log.error("Error registering student", e);
            return ResponseEntity.badRequest()
                .body(ApiResponseDto.error("Failed to register student: " + e.getMessage()));
        }
    }

    @GetMapping("/{instituteId}/users/teachers")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Get teachers", description = "Get all teachers in the institute")
    public ResponseEntity<UserListResponseDto> getTeachers(@PathVariable Long instituteId) {
        try {
            List<User> users = userService.getUsersByRoleAndInstitute(UserRole.TEACHER, instituteId);
            
            List<com.testpire.testpire.dto.response.UserResponseDto> userDtos = users.stream()
                .map(com.testpire.testpire.dto.response.UserResponseDto::fromEntity)
                .toList();
            
            UserListResponseDto response = UserListResponseDto.success(
                userDtos, 
                users.size(), 
                0, 
                users.size()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching teachers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UserListResponseDto.error("Failed to fetch teachers: " + e.getMessage()));
        }
    }

    @GetMapping("/{instituteId}/users/students")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN})
    @Operation(summary = "Get students", description = "Get all students in the institute")
    public ResponseEntity<UserListResponseDto> getStudents(@PathVariable Long instituteId) {
        try {
            List<User> users = userService.getUsersByRoleAndInstitute(UserRole.STUDENT, instituteId);
            
            List<com.testpire.testpire.dto.response.UserResponseDto> userDtos = users.stream()
                .map(com.testpire.testpire.dto.response.UserResponseDto::fromEntity)
                .toList();
            
            UserListResponseDto response = UserListResponseDto.success(
                userDtos, 
                users.size(), 
                0, 
                users.size()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UserListResponseDto.error("Failed to fetch students: " + e.getMessage()));
        }
    }
}