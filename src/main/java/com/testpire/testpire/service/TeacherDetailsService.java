package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.TeacherSearchRequestDto;
import com.testpire.testpire.dto.response.TeacherListResponseDto;
import com.testpire.testpire.dto.response.TeacherResponseDto;
import com.testpire.testpire.entity.TeacherDetails;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.repository.TeacherDetailsRepository;
import com.testpire.testpire.repository.specification.TeacherSpecification;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherDetailsService {

    private final TeacherDetailsRepository teacherDetailsRepository;

    public TeacherDetails createTeacherDetails(User user, String phone, String department, String subject) {
        log.info("Creating teacher details for user ID: {}", user.getId());
        
        TeacherDetails teacherDetails = TeacherDetails.builder()
                .user(user)
                .phone(phone)
                .department(department)
                .subject(subject)
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        TeacherDetails savedDetails = teacherDetailsRepository.save(teacherDetails);
        log.info("Teacher details created successfully with ID: {}", savedDetails.getId());
        return savedDetails;
    }

    public TeacherDetails updateTeacherDetails(User user, String phone, String department, String subject,
                                             String qualification, Integer experienceYears, String specialization, String bio) {
        log.info("Updating teacher details for user ID: {}", user.getId());
        
        TeacherDetails teacherDetails = teacherDetailsRepository.findByUserId(user.getId())
                .orElse(TeacherDetails.builder().user(user).build());

        if (StringUtils.isNoneBlank(phone)) {
            teacherDetails.setPhone(phone);
        }
        if (StringUtils.isNoneBlank(department)) {
            teacherDetails.setDepartment(department);
        }
        if ( StringUtils.isNoneBlank(subject)){
            teacherDetails.setSubject(subject);
        }
        if (qualification != null) {
            teacherDetails.setQualification(qualification);
        }
        if (experienceYears != null) {
            teacherDetails.setExperienceYears(experienceYears);
        }
        if (specialization != null) {
            teacherDetails.setSpecialization(specialization);
        }
        if (bio != null) {
            teacherDetails.setBio(bio);
        }
        
        teacherDetails.setUpdatedBy(RequestUtils.getCurrentUsername());

        TeacherDetails savedDetails = teacherDetailsRepository.save(teacherDetails);
        log.info("Teacher details updated successfully for user ID: {}", user.getId());
        return savedDetails;
    }

    public Optional<TeacherDetails> getTeacherDetailsByUserId(Long userId) {
        return teacherDetailsRepository.findByUserId(userId);
    }

    public Optional<TeacherDetails> getTeacherDetailsByUser(User user) {
        return teacherDetailsRepository.findByUser(user);
    }

    public List<TeacherDetails> getTeachersByInstitute(Long instituteId) {
        return teacherDetailsRepository.findByInstituteId(instituteId);
    }

    public List<TeacherDetails> getTeachersByDepartment(String department) {
        return teacherDetailsRepository.findByDepartment(department);
    }

    public List<TeacherDetails> getTeachersBySubject(String subject) {
        return teacherDetailsRepository.findBySubject(subject);
    }

    public List<TeacherDetails> searchTeachers(String query) {
        return teacherDetailsRepository.searchTeachers(query);
    }

    public List<TeacherDetails> searchTeachersByInstitute(Long instituteId, String query) {
        return teacherDetailsRepository.searchTeachersByInstitute(instituteId, query);
    }

    public void deleteTeacherDetails(Long userId) {
        log.info("Deleting teacher details for user ID: {}", userId);
        
        TeacherDetails teacherDetails = teacherDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher details not found for user ID: " + userId));
        
        teacherDetailsRepository.delete(teacherDetails);
        log.info("Teacher details deleted successfully for user ID: {}", userId);
    }
    
    public TeacherListResponseDto searchTeachersWithSpecification(TeacherSearchRequestDto request) {
        log.info("Searching teachers with specification: {}", request);
        log.info("Institute ID from request: {}", request.getInstituteId());
        
        // Build specification
        Specification<TeacherDetails> spec = Specification.where(TeacherSpecification.isTeacher())
                .and(TeacherSpecification.hasInstituteId(request.getInstituteId()))
                .and(TeacherSpecification.hasSearchText(request.getSearchText()))
                .and(TeacherSpecification.hasFirstNameContaining(request.getFirstName()))
                .and(TeacherSpecification.hasLastNameContaining(request.getLastName()))
                .and(TeacherSpecification.hasUsernameContaining(request.getUsername()))
                .and(TeacherSpecification.hasEmailContaining(request.getEmail()))
                .and(TeacherSpecification.hasPhoneContaining(request.getPhone()))
                .and(TeacherSpecification.hasDepartment(request.getDepartment()))
                .and(TeacherSpecification.hasSubject(request.getSubject()))
                .and(TeacherSpecification.hasQualificationContaining(request.getQualification()))
                .and(TeacherSpecification.hasExperienceYearsRange(request.getMinExperienceYears(), request.getMaxExperienceYears()))
                .and(TeacherSpecification.hasSpecializationContaining(request.getSpecialization()))
                .and(TeacherSpecification.hasBioContaining(request.getBio()))
                .and(TeacherSpecification.isEnabled(request.getEnabled()))
                .and(TeacherSpecification.createdAfter(request.getCreatedAfter()))
                .and(TeacherSpecification.createdBefore(request.getCreatedBefore()))
                .and(TeacherSpecification.createdBy(request.getCreatedBy()));
        
        // Create pageable
        Sort sort = Sort.by(
            Sort.Direction.fromString(request.getSortDirection()),
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // Execute query
        Page<TeacherDetails> page = teacherDetailsRepository.findAll(spec, pageable);
        log.info("Found {} teachers out of {} total", page.getContent().size(), page.getTotalElements());
        
        // Additional debug: Check if there are any teachers at all
        long totalCount = teacherDetailsRepository.count();
        log.info("Total teachers in database: {}", totalCount);
        
        if (totalCount == 0) {
            log.warn("No teachers found in database at all!");
        } else {
            // Check if there are teachers with the correct role
            List<TeacherDetails> allTeachers = teacherDetailsRepository.findAll();
            long teacherRoleCount = allTeachers.stream()
                .filter(t -> t.getUser().getRole().name().equals("TEACHER"))
                .count();
            log.info("Teachers with TEACHER role: {}", teacherRoleCount);
            
            // Check if there are teachers with the correct institute ID
            long instituteCount = allTeachers.stream()
                .filter(t -> t.getUser().getInstituteId().equals(request.getInstituteId()))
                .count();
            log.info("Teachers with institute ID {}: {}", request.getInstituteId(), instituteCount);
        }
        
        // Convert to DTOs
        List<TeacherResponseDto> teacherDtos = page.getContent().stream()
                .map(details -> TeacherResponseDto.fromEntity(details.getUser(), details))
                .toList();
        
        return TeacherListResponseDto.success(
            teacherDtos,
            teacherDtos.size(),
            page.getNumber(),
            (int) page.getTotalElements()
        );
    }
    
    // Debug method to check if there are any teachers in the database
    public void debugTeacherCount() {
        long totalTeachers = teacherDetailsRepository.count();
        log.info("Total teachers in database: {}", totalTeachers);
        
        // Check teachers by role
        List<TeacherDetails> allTeachers = teacherDetailsRepository.findAll();
        log.info("All teachers: {}", allTeachers.size());
        
        for (TeacherDetails teacher : allTeachers) {
            log.info("Teacher: {} - Role: {} - InstituteId: {}", 
                teacher.getUser().getUsername(), 
                teacher.getUser().getRole(), 
                teacher.getUser().getInstituteId());
        }
        
        // Test specification with institute ID 3
        log.info("Testing specification with institute ID 3...");
        Specification<TeacherDetails> spec = Specification.where(TeacherSpecification.isTeacher())
                .and(TeacherSpecification.hasInstituteId(3L));
        
        List<TeacherDetails> teachersWithSpec = teacherDetailsRepository.findAll(spec);
        log.info("Teachers found with specification (instituteId=3): {}", teachersWithSpec.size());
        
        for (TeacherDetails teacher : teachersWithSpec) {
            log.info("Specification result - Teacher: {} - Role: {} - InstituteId: {}", 
                teacher.getUser().getUsername(), 
                teacher.getUser().getRole(), 
                teacher.getUser().getInstituteId());
        }
    }
}
