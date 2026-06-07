package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.StudentSearchRequestDto;
import com.testpire.testpire.dto.response.EnrollmentResponseDto;
import com.testpire.testpire.dto.response.StudentListResponseDto;
import com.testpire.testpire.dto.response.StudentResponseDto;
import com.testpire.testpire.entity.StudentDetails;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.Gender;
import com.testpire.testpire.repository.StudentDetailsRepository;
import com.testpire.testpire.repository.specification.StudentSpecification;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentDetailsService {

    private final StudentDetailsRepository studentDetailsRepository;
    private final StudentEnrollmentService studentEnrollmentService;

    public StudentDetails createStudentDetails(User user, String phone, String course, Integer currentClass,
                                             Gender gender, String rollNumber, String parentName, String parentPhone,
                                             String parentEmail, String address, LocalDate dateOfBirth,
                                             String bloodGroup, String emergencyContact) {
        log.info("Creating student details for user ID: {}", user.getId());

        StudentDetails studentDetails = StudentDetails.builder()
                .user(user)
                .phone(phone)
                .course(course)
                .currentClass(currentClass)
                .gender(gender)
                .rollNumber(rollNumber)
                .parentName(parentName)
                .parentPhone(parentPhone)
                .parentEmail(parentEmail)
                .address(address)
                .dateOfBirth(dateOfBirth)
                .bloodGroup(bloodGroup)
                .emergencyContact(emergencyContact)
                .createdBy(RequestUtils.getCurrentUsername())
                .build();

        StudentDetails savedDetails = studentDetailsRepository.save(studentDetails);
        log.info("Student details created successfully with ID: {}", savedDetails.getId());
        return savedDetails;
    }

    public StudentDetails updateStudentDetails(User user, String phone, String course, Integer currentClass,
                                             Gender gender, String rollNumber, String parentName, String parentPhone,
                                             String parentEmail, String address, LocalDate dateOfBirth,
                                             String bloodGroup, String emergencyContact) {
        log.info("Updating student details for user ID: {}", user.getId());

        StudentDetails studentDetails = studentDetailsRepository.findByUserId(user.getId())
                .orElse(StudentDetails.builder().user(user).build());

        if (phone != null) {
            studentDetails.setPhone(phone);
        }
        if (course != null) {
            studentDetails.setCourse(course);
        }
        if (currentClass != null) {
            studentDetails.setCurrentClass(currentClass);
        }
        if (gender != null) {
            studentDetails.setGender(gender);
        }
        if (rollNumber != null) {
            studentDetails.setRollNumber(rollNumber);
        }
        if (parentName != null) {
            studentDetails.setParentName(parentName);
        }
        if (parentPhone != null) {
            studentDetails.setParentPhone(parentPhone);
        }
        if (parentEmail != null) {
            studentDetails.setParentEmail(parentEmail);
        }
        if (address != null) {
            studentDetails.setAddress(address);
        }
        if (dateOfBirth != null) {
            studentDetails.setDateOfBirth(dateOfBirth);
        }
        if (bloodGroup != null) {
            studentDetails.setBloodGroup(bloodGroup);
        }
        if (emergencyContact != null) {
            studentDetails.setEmergencyContact(emergencyContact);
        }
        
        studentDetails.setUpdatedBy(RequestUtils.getCurrentUsername());

        StudentDetails savedDetails = studentDetailsRepository.save(studentDetails);
        log.info("Student details updated successfully for user ID: {}", user.getId());
        return savedDetails;
    }

    public Optional<StudentDetails> getStudentDetailsByUserId(Long userId) {
        return studentDetailsRepository.findByUserId(userId);
    }

    public Optional<StudentDetails> getStudentDetailsByUser(User user) {
        return studentDetailsRepository.findByUser(user);
    }

    public List<StudentDetails> getStudentsByInstitute(Long instituteId) {
        return studentDetailsRepository.findByInstituteId(instituteId);
    }

    public List<StudentDetails> getStudentsByCourse(String course) {
        return studentDetailsRepository.findByCourse(course);
    }

    public List<StudentDetails> getStudentsByInstituteAndCourse(Long instituteId, String course) {
        return studentDetailsRepository.findByInstituteIdAndCourse(instituteId, course);
    }

    public List<StudentDetails> getStudentsByBatch(Long batchId) {
        return studentDetailsRepository.findByBatchId(batchId);
    }

    public List<StudentDetails> searchStudents(String query) {
        return studentDetailsRepository.searchStudents(query);
    }

    public List<StudentDetails> searchStudentsByInstitute(Long instituteId, String query) {
        return studentDetailsRepository.searchStudentsByInstitute(instituteId, query);
    }

    public List<StudentDetails> searchStudentsByInstituteAndCourse(Long instituteId, String course, String query) {
        return studentDetailsRepository.searchStudentsByInstituteAndCourse(instituteId, course, query);
    }

    public void deleteStudentDetails(Long userId) {
        log.info("Deleting student details for user ID: {}", userId);
        
        StudentDetails studentDetails = studentDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Student details not found for user ID: " + userId));
        
        studentDetailsRepository.delete(studentDetails);
        log.info("Student details deleted successfully for user ID: {}", userId);
    }
    
    public StudentListResponseDto searchStudentsWithSpecification(StudentSearchRequestDto request) {
        Specification<StudentDetails> spec = Specification.where(StudentSpecification.isStudent())
                .and(StudentSpecification.hasInstituteId(request.getInstituteId()))
                .and(StudentSpecification.hasSearchText(request.getSearchText()))
                .and(StudentSpecification.hasFirstNameContaining(request.getFirstName()))
                .and(StudentSpecification.hasLastNameContaining(request.getLastName()))
                .and(StudentSpecification.hasUsernameContaining(request.getUsername()))
                .and(StudentSpecification.hasEmailContaining(request.getEmail()))
                .and(StudentSpecification.hasPhoneContaining(request.getPhone()))
                .and(StudentSpecification.hasCourse(request.getCourse()))
                .and(StudentSpecification.hasEnrollmentInCourse(request.getCourseId()))
                .and(StudentSpecification.hasEnrollmentInBatch(request.getBatchId()))
                .and(StudentSpecification.hasCurrentClassRange(request.getMinCurrentClass(), request.getMaxCurrentClass()))
                .and(StudentSpecification.hasRollNumberContaining(request.getRollNumber()))
                .and(StudentSpecification.hasParentNameContaining(request.getParentName()))
                .and(StudentSpecification.hasParentPhoneContaining(request.getParentPhone()))
                .and(StudentSpecification.hasParentEmailContaining(request.getParentEmail()))
                .and(StudentSpecification.hasAddressContaining(request.getAddress()))
                .and(StudentSpecification.hasBloodGroup(request.getBloodGroup()))
                .and(StudentSpecification.hasEmergencyContactContaining(request.getEmergencyContact()))
                .and(StudentSpecification.isEnabled(request.getEnabled()))
                .and(StudentSpecification.createdAfter(request.getCreatedAfter()))
                .and(StudentSpecification.createdBefore(request.getCreatedBefore()))
                .and(StudentSpecification.createdBy(request.getCreatedBy()));

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<StudentDetails> page = studentDetailsRepository.findAll(spec, pageable);
        log.info("Student search returned {}/{} total", page.getContent().size(), page.getTotalElements());

        List<StudentResponseDto> studentDtos = toResponsesWithEnrollments(page.getContent());

        return StudentListResponseDto.success(
            studentDtos,
            studentDtos.size(),
            page.getNumber(),
            (int) page.getTotalElements()
        );
    }

    /**
     * Maps a list of students to response DTOs with their enrolled course/batch lists populated. Use
     * this for list/search results so callers (the UI) get each student's enrollments, not an empty
     * list. Enrollments are resolved in bulk (a fixed number of queries), not per student.
     */
    @Transactional(readOnly = true)
    public List<StudentResponseDto> toResponsesWithEnrollments(List<StudentDetails> students) {
        List<Long> userIds = students.stream().map(d -> d.getUser().getId()).toList();
        Map<Long, List<EnrollmentResponseDto>> byStudent =
            studentEnrollmentService.getEnrollmentsForStudents(userIds);
        return students.stream()
            .map(d -> StudentResponseDto.fromEntity(d.getUser(), d,
                byStudent.getOrDefault(d.getUser().getId(), List.of())))
            .toList();
    }
}
