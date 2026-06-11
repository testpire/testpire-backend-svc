package com.testpire.testpire.service;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.dto.InstituteDto;
import com.testpire.testpire.dto.request.InstituteSearchRequestDto;
import com.testpire.testpire.dto.response.InstituteListResponseDto;
import com.testpire.testpire.dto.response.InstituteResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.repository.InstituteRepository;
import com.testpire.testpire.repository.LeadRepository;
import com.testpire.testpire.repository.QuestionRepository;
import com.testpire.testpire.repository.TestAttemptRepository;
import com.testpire.testpire.repository.TestRepository;
import com.testpire.testpire.repository.UserRepository;
import com.testpire.testpire.repository.specification.InstituteSpecification;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstituteService {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final TestRepository testRepository;
    private final LeadRepository leadRepository;
    private final QuestionRepository questionRepository;
    private final CognitoService cognitoService;

    public Institute createInstitute(InstituteDto instituteDto, String createdBy) {
        log.info("Creating institute: {}", instituteDto.name());
        
        // Check if institute code already exists
        if (instituteRepository.existsByCode(instituteDto.code())) {
            throw new IllegalArgumentException(String.format(ApplicationConstants.Messages.INSTITUTE_ALREADY_EXISTS, instituteDto.code()));
        }
        
        // Check if email already exists
        if (instituteRepository.existsByEmail(instituteDto.email())) {
            throw new IllegalArgumentException(String.format(ApplicationConstants.Messages.INSTITUTE_EMAIL_EXISTS, instituteDto.email()));
        }

        Institute institute = Institute.builder()
                .code(instituteDto.code())
                .name(instituteDto.name())
                .address(instituteDto.address())
                .city(instituteDto.city())
                .state(instituteDto.state())
                .country(instituteDto.country())
                .postalCode(instituteDto.postalCode())
                .phone(instituteDto.phone())
                .email(instituteDto.email())
                .website(instituteDto.website())
                .description(instituteDto.description())
                .createdBy(createdBy)
                .build();

        Institute savedInstitute = instituteRepository.save(institute);
        log.info("Institute created successfully with ID: {}", savedInstitute.getId());
        return savedInstitute;
    }

    public Institute updateInstitute(Long id, InstituteDto instituteDto, String updatedBy) {
        log.info("Updating institute with ID: {}", id);
        
        Institute existingInstitute = instituteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));

        // Check if code is being changed and if new code already exists
        if (!existingInstitute.getCode().equals(instituteDto.code()) && 
            instituteRepository.existsByCode(instituteDto.code())) {
            throw new IllegalArgumentException("Institute with code " + instituteDto.code() + " already exists");
        }

        // Check if email is being changed and if new email already exists
        if (!existingInstitute.getEmail().equals(instituteDto.email()) && 
            instituteRepository.existsByEmail(instituteDto.email())) {
            throw new IllegalArgumentException("Institute with email " + instituteDto.email() + " already exists");
        }

        existingInstitute.setCode(instituteDto.code());
        existingInstitute.setName(instituteDto.name());
        existingInstitute.setAddress(instituteDto.address());
        existingInstitute.setCity(instituteDto.city());
        existingInstitute.setState(instituteDto.state());
        existingInstitute.setCountry(instituteDto.country());
        existingInstitute.setPostalCode(instituteDto.postalCode());
        existingInstitute.setPhone(instituteDto.phone());
        existingInstitute.setEmail(instituteDto.email());
        existingInstitute.setWebsite(instituteDto.website());
        existingInstitute.setDescription(instituteDto.description());
        existingInstitute.setUpdatedBy(updatedBy);

        Institute updatedInstitute = instituteRepository.save(existingInstitute);
        log.info("Institute updated successfully with ID: {}", updatedInstitute.getId());
        return updatedInstitute;
    }

    /**
     * Hard-deletes an institute and <em>everything</em> belonging to it. This is a full teardown,
     * not a simple {@code DELETE FROM institutes}, because:
     * <ul>
     *   <li>{@code leads.institute_id} has no {@code ON DELETE} rule, so it would block the delete;</li>
     *   <li>{@code users} has no FK to {@code institutes} (and the accounts also live in Cognito), so
     *       they would be silently orphaned;</li>
     *   <li>question references in {@code test_questions}/{@code test_attempt_answers} are
     *       {@code ON DELETE RESTRICT} (V27), so a single institute cascade would race the
     *       institute&rarr;questions and institute&rarr;tests cascades nondeterministically.</li>
     * </ul>
     * We therefore delete children in an explicit, FK-safe order — results and test-question links
     * <strong>before</strong> questions — keeping the V27 protection intact for the question-bank
     * delete path. The remaining clean hierarchy (courses/subjects/chapters/topics/batches/
     * enrollments/test_assignments) is dropped by the institute row's own {@code ON DELETE CASCADE}.
     *
     * <p>The whole operation runs in one transaction (class-level {@code @Transactional}). Cognito
     * account deletion is best-effort per user and is <em>not</em> transactional: if a later DB step
     * fails and rolls back, already-removed Cognito accounts stay removed.</p>
     */
    public void deleteInstitute(Long id) {
        log.info("Deleting institute with ID: {} (full teardown)", id);

        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));

        // 1. Attempts + answers (DB cascade) — clears RESTRICT refs from test_attempt_answers -> questions.
        testAttemptRepository.deleteByInstituteId(id);
        // 2. Tests + test_questions (DB cascade) — clears RESTRICT refs from test_questions -> questions.
        testRepository.deleteByInstituteId(id);
        // 3. Leads — the original blocker (no ON DELETE rule on leads.institute_id).
        leadRepository.deleteByInstituteId(id);
        // 4. Questions + options (DB cascade) — now safe, no surviving RESTRICT references.
        questionRepository.deleteByInstituteId(id);

        // 5. Users: remove from Cognito (best-effort), then from the DB (DB cascade removes
        //    teacher_details / student_details / student_enrollments and any leftover attempts).
        List<User> users = userRepository.findByInstituteId(id);
        for (User user : users) {
            try {
                cognitoService.deleteUser(user.getUsername());
            } catch (Exception e) {
                log.warn("Failed to delete Cognito user '{}' during institute {} teardown; continuing. Reason: {}",
                        user.getUsername(), id, e.getMessage());
            }
        }
        userRepository.deleteAll(users);
        log.info("Removed {} user(s) for institute {}", users.size(), id);

        // 6. Institute — cascades the remaining hierarchy (courses, subjects, chapters, topics,
        //    batches, student_enrollments, test_assignments).
        instituteRepository.delete(institute);
        log.info("Institute deleted successfully with ID: {}", id);
    }

    public Institute getInstituteById(Long id) {
        return instituteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with ID: " + id));
    }

    public Institute getInstituteByCode(String code) {
        return instituteRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Institute not found with code: " + code));
    }

    public List<Institute> getAllInstitutes() {
        return instituteRepository.findAll();
    }

    public List<Institute> searchInstitutes(String searchTerm) {
        return instituteRepository.searchByNameOrCode(searchTerm);
    }

    public boolean instituteExistsByCode(String code) {
        return instituteRepository.existsByCode(code);
    }

    public boolean instituteExistsById(Long id) {
        return instituteRepository.existsById(id);
    }

    public InstituteListResponseDto searchInstitutesWithSpecification(InstituteSearchRequestDto request) {
        log.info("Searching institutes with specification: {}", request);
        
        // Build specification
        Specification<Institute> spec = Specification.where(InstituteSpecification.hasSearchText(request.getCriteria().getSearchText()))
            .and(InstituteSpecification.hasInstituteId(request.getCriteria().getInstituteId()))
                .and(InstituteSpecification.hasNameContaining(request.getCriteria().getName()))
                .and(InstituteSpecification.hasCodeContaining(request.getCriteria().getCode()))
                .and(InstituteSpecification.hasAddressContaining(request.getCriteria().getAddress()))
                .and(InstituteSpecification.hasCity(request.getCriteria().getCity()))
                .and(InstituteSpecification.hasState(request.getCriteria().getState()))
                .and(InstituteSpecification.hasCountry(request.getCriteria().getCountry()))
                .and(InstituteSpecification.hasPostalCodeContaining(request.getCriteria().getPostalCode()))
                .and(InstituteSpecification.hasPhoneContaining(request.getCriteria().getPhone()))
                .and(InstituteSpecification.hasEmailContaining(request.getCriteria().getEmail()))
                .and(InstituteSpecification.hasWebsiteContaining(request.getCriteria().getWebsite()))
                .and(InstituteSpecification.hasDescriptionContaining(request.getCriteria().getDescription()))
                .and(InstituteSpecification.createdAfter(request.getCriteria().getCreatedAfter()))
                .and(InstituteSpecification.createdBefore(request.getCriteria().getCreatedBefore()))
                .and(InstituteSpecification.createdBy(request.getCriteria().getCreatedBy()))
                .and(InstituteSpecification.updatedBy(request.getCriteria().getUpdatedBy()));
        
        // Create pageable
        Sort sort = Sort.by(
            Sort.Direction.fromString(request.getSorting().getDirection()),
            request.getSorting().getField()
        );
        Pageable pageable = PageRequest.of(request.getPagination().getPage(), request.getPagination().getSize(), sort);
        
        // Execute query
        Page<Institute> page = instituteRepository.findAll(spec, pageable);
        log.info("Found {} institutes out of {} total", page.getContent().size(), page.getTotalElements());
        
        // Convert to DTOs
        List<InstituteResponseDto> instituteDtos = page.getContent().stream()
                .map(InstituteResponseDto::fromEntity)
                .toList();
        
        return InstituteListResponseDto.success(
            instituteDtos,
            instituteDtos.size(),
            page.getNumber(),
            (int) page.getTotalElements()
        );
    }
} 