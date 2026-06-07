package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.ConvertLeadRequestDto;
import com.testpire.testpire.dto.request.CreateLeadRequestDto;
import com.testpire.testpire.dto.request.LeadSearchRequestDto;
import com.testpire.testpire.dto.request.UpdateLeadRequestDto;
import com.testpire.testpire.dto.response.LeadListResponseDto;
import com.testpire.testpire.dto.response.LeadResponseDto;
import com.testpire.testpire.entity.Course;
import com.testpire.testpire.entity.Lead;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.LeadStatus;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.repository.CourseRepository;
import com.testpire.testpire.repository.LeadRepository;
import com.testpire.testpire.repository.specification.LeadSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages the enquiry/lead pipeline and the conversion of a lead into a real student.
 *
 * <p>A {@code null} instituteId argument means the caller is SUPER_ADMIN operating cross-institute;
 * any non-null value scopes the operation to that institute (multi-tenancy isolation). Controllers
 * resolve the effective institute via {@code RequestUtils.resolveInstituteId} before calling here.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadService {

    private final LeadRepository leadRepository;
    private final CourseRepository courseRepository;
    private final CognitoService cognitoService;
    private final UserService userService;
    private final StudentDetailsService studentDetailsService;
    private final StudentEnrollmentService studentEnrollmentService;

    public Lead createLead(CreateLeadRequestDto request, Long instituteId, String actor) {
        log.info("Creating lead for institute {}: {} {}", instituteId, request.firstName(), request.lastName());

        if (request.interestedCourseId() != null
            && courseRepository.findByIdAndInstituteId(request.interestedCourseId(), instituteId).isEmpty()) {
            throw new IllegalArgumentException(
                "Course not found in this institute with ID: " + request.interestedCourseId());
        }

        Lead lead = Lead.builder()
            .instituteId(instituteId)
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .phone(request.phone())
            .gender(request.gender())
            .school(request.school())
            .currentClass(request.currentClass())
            .board(request.board())
            .courseFeeCommitted(request.courseFeeCommitted())
            .parentName(request.parentName())
            .parentPhone(request.parentPhone())
            .parentEmail(request.parentEmail())
            .status(LeadStatus.NEW)
            .source(request.source())
            .interestedCourseId(request.interestedCourseId())
            .assignedTo(request.assignedTo())
            .nextFollowUpDate(request.nextFollowUpDate())
            .notes(request.notes())
            .createdBy(actor)
            .build();

        Lead saved = leadRepository.save(lead);
        log.info("Lead created with ID: {}", saved.getId());
        return saved;
    }

    public Lead getLead(Long id, Long instituteId) {
        return findScoped(id, instituteId);
    }

    public Lead updateLead(Long id, UpdateLeadRequestDto request, Long instituteId, String actor) {
        Lead lead = findScoped(id, instituteId);

        if (request.status() == LeadStatus.ENROLLED) {
            throw new IllegalArgumentException(
                "Cannot set status to ENROLLED directly; use the convert endpoint to enroll a lead");
        }
        if (lead.getConvertedUserId() != null && request.status() != null
            && request.status() != LeadStatus.ENROLLED) {
            throw new IllegalArgumentException("Lead is already converted; its status cannot be changed");
        }
        if (request.interestedCourseId() != null
            && courseRepository.findByIdAndInstituteId(request.interestedCourseId(), lead.getInstituteId()).isEmpty()) {
            throw new IllegalArgumentException(
                "Course not found in this institute with ID: " + request.interestedCourseId());
        }

        if (request.firstName() != null) lead.setFirstName(request.firstName());
        if (request.lastName() != null) lead.setLastName(request.lastName());
        if (request.email() != null) lead.setEmail(request.email());
        if (request.phone() != null) lead.setPhone(request.phone());
        if (request.gender() != null) lead.setGender(request.gender());
        if (request.school() != null) lead.setSchool(request.school());
        if (request.currentClass() != null) lead.setCurrentClass(request.currentClass());
        if (request.board() != null) lead.setBoard(request.board());
        if (request.courseFeeCommitted() != null) lead.setCourseFeeCommitted(request.courseFeeCommitted());
        if (request.parentName() != null) lead.setParentName(request.parentName());
        if (request.parentPhone() != null) lead.setParentPhone(request.parentPhone());
        if (request.parentEmail() != null) lead.setParentEmail(request.parentEmail());
        if (request.status() != null) lead.setStatus(request.status());
        if (request.source() != null) lead.setSource(request.source());
        if (request.interestedCourseId() != null) lead.setInterestedCourseId(request.interestedCourseId());
        if (request.assignedTo() != null) lead.setAssignedTo(request.assignedTo());
        if (request.nextFollowUpDate() != null) lead.setNextFollowUpDate(request.nextFollowUpDate());
        if (request.notes() != null) lead.setNotes(request.notes());
        lead.setUpdatedBy(actor);

        return leadRepository.save(lead);
    }

    public void deleteLead(Long id, Long instituteId) {
        Lead lead = findScoped(id, instituteId);
        leadRepository.delete(lead);
        log.info("Lead deleted with ID: {}", id);
    }

    /**
     * Enrolls a lead: provisions the Cognito user + {@code User} + {@code StudentDetails} (Cognito
     * emails the temporary password), then marks the lead ENROLLED and links it to the new student.
     *
     * <p>Like the existing student-create path, the Cognito call precedes the DB writes; if a DB
     * write fails afterward the Cognito account is left orphaned. Behavior is intentionally kept in
     * parity with {@code StudentController.createStudent} rather than adding compensation here.</p>
     */
    public Lead convertLead(Long id, ConvertLeadRequestDto request, Long instituteId, String actor) {
        Lead lead = findScoped(id, instituteId);

        if (lead.getConvertedUserId() != null) {
            throw new IllegalArgumentException("Lead has already been converted to a student");
        }

        Course course = courseRepository.findByIdAndInstituteId(request.enrolledCourseId(), lead.getInstituteId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Course not found in this institute with ID: " + request.enrolledCourseId()));

        String email = request.email() != null ? request.email() : lead.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "An email is required to convert a lead (provide one in the request or on the lead)");
        }

        String lastName = lead.getLastName() != null ? lead.getLastName() : "";

        log.info("Converting lead {} into a student (institute {}, course {})",
            id, lead.getInstituteId(), course.getId());

        // 1. Cognito account (emails a temp password to the user).
        String cognitoUserId = cognitoService.adminCreateUser(
            email, lead.getFirstName(), lastName, UserRole.STUDENT, lead.getInstituteId());

        // 2. Local user.
        User user = userService.createUser(
            email, lead.getFirstName(), lastName,
            UserRole.STUDENT, lead.getInstituteId(), cognitoUserId, actor);

        // 3. Student details. Academic/demographic fields and parent contact are carried over
        //    from the lead (captured at enquiry time); the rest come from the convert request.
        studentDetailsService.createStudentDetails(
            user,
            lead.getPhone(),
            course.getName(),
            lead.getCurrentClass(),
            lead.getGender(),
            request.rollNumber(),
            lead.getParentName(),
            lead.getParentPhone(),
            lead.getParentEmail(),
            request.address(),
            request.dateOfBirth(),
            request.bloodGroup(),
            request.emergencyContact());

        // 3b. If a batch was chosen, create the course+batch enrollment for the new student.
        if (request.enrolledBatchId() != null) {
            studentEnrollmentService.addEnrollment(
                user.getId(), lead.getInstituteId(), course.getId(), request.enrolledBatchId(), actor);
        }

        // 4. Close the lead and link it to the provisioned student.
        lead.setStatus(LeadStatus.ENROLLED);
        lead.setConvertedUserId(user.getId());
        lead.setEnrolledCourseId(course.getId());
        lead.setUpdatedBy(actor);
        Lead saved = leadRepository.save(lead);

        log.info("Lead {} converted to student userId {}", id, user.getId());
        return saved;
    }

    public LeadListResponseDto search(LeadSearchRequestDto request) {
        Specification<Lead> spec = Specification.where(LeadSpecification.hasInstituteId(request.getInstituteId()))
            .and(LeadSpecification.hasSearchText(request.getSearchText()))
            .and(LeadSpecification.hasFirstNameContaining(request.getFirstName()))
            .and(LeadSpecification.hasLastNameContaining(request.getLastName()))
            .and(LeadSpecification.hasEmailContaining(request.getEmail()))
            .and(LeadSpecification.hasPhoneContaining(request.getPhone()))
            .and(LeadSpecification.hasStatus(request.getStatus()))
            .and(LeadSpecification.hasSource(request.getSource()))
            .and(LeadSpecification.hasInterestedCourseId(request.getInterestedCourseId()))
            .and(LeadSpecification.hasAssignedTo(request.getAssignedTo()))
            .and(LeadSpecification.isConverted(request.getConverted()))
            .and(LeadSpecification.followUpFrom(request.getFollowUpFrom()))
            .and(LeadSpecification.followUpTo(request.getFollowUpTo()))
            .and(LeadSpecification.createdAfter(request.getCreatedAfter()))
            .and(LeadSpecification.createdBefore(request.getCreatedBefore()))
            .and(LeadSpecification.createdBy(request.getCreatedBy()));

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Lead> page = leadRepository.findAll(spec, pageable);
        List<LeadResponseDto> leads = page.getContent().stream()
            .map(LeadResponseDto::fromEntity)
            .toList();

        return LeadListResponseDto.success(leads, leads.size(), page.getNumber(), (int) page.getTotalElements());
    }

    /**
     * Loads a lead, enforcing tenant isolation: a non-null instituteId restricts the lookup to that
     * institute (so a non-SUPER_ADMIN cannot reach another tenant's lead); a null instituteId
     * (SUPER_ADMIN) looks up by id alone.
     */
    private Lead findScoped(Long id, Long instituteId) {
        return (instituteId != null
            ? leadRepository.findByIdAndInstituteId(id, instituteId)
            : leadRepository.findById(id))
            .orElseThrow(() -> new IllegalArgumentException("Lead not found with ID: " + id));
    }
}
