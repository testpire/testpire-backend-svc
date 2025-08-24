package com.testpire.testpire.Controller;

import com.testpire.testpire.dto.InstituteDto;
import com.testpire.testpire.mongoDomain.Institute;
import com.testpire.testpire.service.InstituteService;
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
@RequestMapping("/api/institute")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Institute Management", description = "Institute CRUD operations - SUPER_ADMIN only")
public class InstituteController {

    private final InstituteService instituteService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create institute", description = "Create a new institute (SUPER_ADMIN only)")
    public ResponseEntity<?> createInstitute(@Valid @RequestBody InstituteDto instituteDto, 
                                          HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            
            Institute createdInstitute = instituteService.createInstitute(instituteDto, username);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Institute created successfully",
                "instituteId", createdInstitute.getId(),
                "instituteCode", createdInstitute.getCode()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create institute"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update institute", description = "Update an existing institute (SUPER_ADMIN only)")
    public ResponseEntity<?> updateInstitute(@PathVariable Long id, 
                                          @Valid @RequestBody InstituteDto instituteDto,
                                          HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String username = jwtUtil.extractUsername(token);
            
            Institute updatedInstitute = instituteService.updateInstitute(id, instituteDto, username);
            
            return ResponseEntity.ok(Map.of(
                "message", "Institute updated successfully",
                "instituteId", updatedInstitute.getId(),
                "instituteCode", updatedInstitute.getCode()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update institute"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete institute", description = "Deactivate an institute (SUPER_ADMIN only)")
    public ResponseEntity<?> deleteInstitute(@PathVariable Long id) {
        try {
            instituteService.deleteInstitute(id);
            return ResponseEntity.ok(Map.of("message", "Institute deactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to deactivate institute"));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get institute by ID", description = "Get institute details by ID (SUPER_ADMIN only)")
    public ResponseEntity<?> getInstituteById(@PathVariable Long id) {
        try {
            Institute institute = instituteService.getInstituteById(id);
            return ResponseEntity.ok(institute);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch institute"));
        }
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get institute by code", description = "Get institute details by code (SUPER_ADMIN only)")
    public ResponseEntity<?> getInstituteByCode(@PathVariable String code) {
        try {
            Institute institute = instituteService.getInstituteByCode(code);
            return ResponseEntity.ok(institute);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching institute", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch institute"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all institutes", description = "Get all active institutes (SUPER_ADMIN only)")
    public ResponseEntity<?> getAllInstitutes() {
        try {
            List<Institute> institutes = instituteService.getAllActiveInstitutes();
            return ResponseEntity.ok(institutes);
        } catch (Exception e) {
            log.error("Error fetching institutes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch institutes"));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Search institutes", description = "Search institutes by name or code (SUPER_ADMIN only)")
    public ResponseEntity<?> searchInstitutes(@RequestParam String searchTerm) {
        try {
            List<Institute> institutes = instituteService.searchInstitutes(searchTerm);
            return ResponseEntity.ok(institutes);
        } catch (Exception e) {
            log.error("Error searching institutes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to search institutes"));
        }
    }
} 