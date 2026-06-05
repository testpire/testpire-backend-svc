package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.ConvertLeadRequestDto;
import com.testpire.testpire.dto.request.CreateLeadRequestDto;
import com.testpire.testpire.dto.request.LeadCriteriaDto;
import com.testpire.testpire.dto.request.LeadSearchRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.request.UpdateLeadRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.LeadListResponseDto;
import com.testpire.testpire.dto.response.LeadResponseDto;
import com.testpire.testpire.entity.Lead;
import com.testpire.testpire.enums.LeadSource;
import com.testpire.testpire.enums.LeadStatus;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.LeadService;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Lead / enquiry pipeline. Leads are CRM records with no login account; the convert endpoint
 * provisions a real student (Cognito + User + StudentDetails) and links it back to the lead.
 *
 * <p>Permission tiers (seeded in V16): create/read/list/search/update are TEACHER+; convert and
 * delete are INST_ADMIN+, matching the floor for creating a student directly.</p>
 */
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lead Management", description = "Enquiry/lead pipeline and conversion to student")
public class LeadController {

    private final LeadService leadService;
    private final InstituteService instituteService;

    @PostMapping
    @RequirePermission(Permission.LEAD_CREATE)
    @Operation(summary = "Create lead", description = "Capture a new enquiry (no login account is created)")
    public ResponseEntity<ApiResponseDto> createLead(@Valid @RequestBody CreateLeadRequestDto request) {
        try {
            Long instituteId = RequestUtils.resolveInstituteId(request.instituteId());
            if (instituteId == null) {
                return ResponseEntity.badRequest().body(ApiResponseDto.error("Institute ID is required"));
            }
            if (!instituteService.instituteExistsById(instituteId)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Institute not found with ID: " + instituteId));
            }

            Lead lead = leadService.createLead(request, instituteId, RequestUtils.getCurrentUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Lead created successfully", LeadResponseDto.fromEntity(lead)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating lead", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create lead: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(Permission.LEAD_READ)
    @Operation(summary = "Get lead by ID", description = "Get a lead by ID")
    public ResponseEntity<ApiResponseDto> getLead(@PathVariable Long id) {
        try {
            Lead lead = leadService.getLead(id, RequestUtils.resolveInstituteId(null));
            return ResponseEntity.ok(
                ApiResponseDto.success("Lead retrieved successfully", LeadResponseDto.fromEntity(lead)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching lead", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to fetch lead: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.LEAD_UPDATE)
    @Operation(summary = "Update lead", description = "Update lead pipeline status and follow-up fields")
    public ResponseEntity<ApiResponseDto> updateLead(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateLeadRequestDto request) {
        try {
            Lead lead = leadService.updateLead(id, request, RequestUtils.resolveInstituteId(null),
                RequestUtils.getCurrentUsername());
            return ResponseEntity.ok(
                ApiResponseDto.success("Lead updated successfully", LeadResponseDto.fromEntity(lead)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating lead", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to update lead: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.LEAD_DELETE)
    @Operation(summary = "Delete lead", description = "Delete a lead (INST_ADMIN or SUPER_ADMIN)")
    public ResponseEntity<ApiResponseDto> deleteLead(@PathVariable Long id) {
        try {
            leadService.deleteLead(id, RequestUtils.resolveInstituteId(null));
            return ResponseEntity.ok(ApiResponseDto.success("Lead deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting lead", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Failed to delete lead: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/convert")
    @RequirePermission(Permission.LEAD_CONVERT)
    @Operation(summary = "Convert lead to student",
        description = "Enroll a lead: creates the Cognito user + student (Cognito emails the password) and links it to the lead (INST_ADMIN or SUPER_ADMIN)")
    public ResponseEntity<ApiResponseDto> convertLead(@PathVariable Long id,
                                                      @Valid @RequestBody ConvertLeadRequestDto request) {
        try {
            Lead lead = leadService.convertLead(id, request, RequestUtils.resolveInstituteId(null),
                RequestUtils.getCurrentUsername());
            return ResponseEntity.ok(ApiResponseDto.success(
                "Lead enrolled and student account created", LeadResponseDto.fromEntity(lead)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error converting lead", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to convert lead: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequirePermission(Permission.LEAD_LIST)
    @Operation(summary = "List leads", description = "List leads with optional filters (status, source, assignee, follow-up window)")
    public ResponseEntity<LeadListResponseDto> listLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) Long interestedCourseId,
            @RequestParam(required = false) Boolean converted,
            @RequestParam(required = false) LocalDate followUpFrom,
            @RequestParam(required = false) LocalDate followUpTo,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        try {
            LeadCriteriaDto criteria = LeadCriteriaDto.builder()
                .instituteId(RequestUtils.resolveInstituteId(null))
                .status(status)
                .source(source)
                .assignedTo(assignedTo)
                .interestedCourseId(interestedCourseId)
                .converted(converted)
                .followUpFrom(followUpFrom)
                .followUpTo(followUpTo)
                .searchText(searchText)
                .build();

            LeadSearchRequestDto request = LeadSearchRequestDto.builder()
                .criteria(criteria)
                .pagination(PaginationRequestDto.builder().page(page).size(size).build())
                .sorting(SortingRequestDto.builder().field(sortBy).direction(sortDirection).build())
                .build();

            return ResponseEntity.ok(leadService.search(request));
        } catch (Exception e) {
            log.error("Error listing leads", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(LeadListResponseDto.error("Failed to list leads: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequirePermission(Permission.LEAD_SEARCH)
    @Operation(summary = "Advanced search leads", description = "Search leads with full criteria, pagination, and sorting")
    public ResponseEntity<ApiResponseDto> searchLeads(@Valid @RequestBody LeadSearchRequestDto request) {
        try {
            // Scope to the caller's institute (non-SUPER_ADMIN); SUPER_ADMIN honors header/criteria.
            Long instituteId = RequestUtils.resolveInstituteId(request.getInstituteId());
            LeadCriteriaDto criteria = request.getCriteria() != null ? request.getCriteria() : new LeadCriteriaDto();
            criteria.setInstituteId(instituteId);
            request.setCriteria(criteria);

            return ResponseEntity.ok(
                ApiResponseDto.success("Leads retrieved successfully", leadService.search(request)));
        } catch (Exception e) {
            log.error("Error in advanced lead search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search leads: " + e.getMessage()));
        }
    }
}
