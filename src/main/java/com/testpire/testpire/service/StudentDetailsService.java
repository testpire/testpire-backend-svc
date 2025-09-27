package com.testpire.testpire.service;

import com.testpire.testpire.dto.request.StudentSearchRequestDto;
import com.testpire.testpire.dto.response.StudentListResponseDto;
import com.testpire.testpire.dto.response.StudentResponseDto;
import com.testpire.testpire.entity.StudentDetails;
import com.testpire.testpire.entity.User;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentDetailsService {

    private final StudentDetailsRepository studentDetailsRepository;

    public StudentDetails createStudentDetails(User user, String phone, String course, Integer yearOfStudy, 
                                             String rollNumber, String parentName, String parentPhone, 
                                             String parentEmail, String address, LocalDateTime dateOfBirth, 
                                             String bloodGroup, String emergencyContact) {
        log.info("Creating student details for user ID: {}", user.getId());
        
        StudentDetails studentDetails = StudentDetails.builder()
                .user(user)
                .phone(phone)
                .course(course)
                .yearOfStudy(yearOfStudy)
                .rollNumber(rollNumber)
                .parentName(parentName)
                .yearOfStudy(2) //todo : fix this
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

    public StudentDetails updateStudentDetails(User user, String phone, String course, Integer yearOfStudy,
                                             String rollNumber, String parentName, String parentPhone, 
                                             String parentEmail, String address, LocalDateTime dateOfBirth, 
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
        if (yearOfStudy != null) {
            studentDetails.setYearOfStudy(yearOfStudy);
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

    public List<StudentDetails> getStudentsByYearOfStudy(Integer yearOfStudy) {
        return studentDetailsRepository.findByYearOfStudy(yearOfStudy);
    }

    public List<StudentDetails> getStudentsByInstituteAndCourse(Long instituteId, String course) {
        return studentDetailsRepository.findByInstituteIdAndCourse(instituteId, course);
    }

    public List<StudentDetails> getStudentsByInstituteAndYearOfStudy(Long instituteId, Integer yearOfStudy) {
        return studentDetailsRepository.findByInstituteIdAndYearOfStudy(instituteId, yearOfStudy);
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
        log.info("Searching students with specification: {}", request);
        log.info("Institute ID from request: {}", request.getInstituteId());
        
        // Build specification
        Specification<StudentDetails> spec = Specification.where(StudentSpecification.isStudent())
                .and(StudentSpecification.hasInstituteId(request.getInstituteId()))
                .and(StudentSpecification.hasSearchText(request.getSearchText()))
                .and(StudentSpecification.hasFirstNameContaining(request.getFirstName()))
                .and(StudentSpecification.hasLastNameContaining(request.getLastName()))
                .and(StudentSpecification.hasUsernameContaining(request.getUsername()))
                .and(StudentSpecification.hasEmailContaining(request.getEmail()))
                .and(StudentSpecification.hasPhoneContaining(request.getPhone()))
                .and(StudentSpecification.hasCourse(request.getCourse()))
                .and(StudentSpecification.hasYearOfStudyRange(request.getMinYearOfStudy(), request.getMaxYearOfStudy()))
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
        
        // Create pageable
        Sort sort = Sort.by(
            Sort.Direction.fromString(request.getSortDirection()),
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // Execute query
        Page<StudentDetails> page = studentDetailsRepository.findAll(spec, pageable);
        log.info("Found {} students out of {} total", page.getContent().size(), page.getTotalElements());
        
        // Additional debug: Check if there are any students at all
        long totalCount = studentDetailsRepository.count();
        log.info("Total students in database: {}", totalCount);
        
        if (totalCount == 0) {
            log.warn("No students found in database at all!");
        } else {
            // Check if there are students with the correct role
            List<StudentDetails> allStudents = studentDetailsRepository.findAll();
            long studentRoleCount = allStudents.stream()
                .filter(s -> s.getUser().getRole().name().equals("STUDENT"))
                .count();
            log.info("Students with STUDENT role: {}", studentRoleCount);
            
            // Check if there are students with the correct institute ID
            long instituteCount = allStudents.stream()
                .filter(s -> s.getUser().getInstituteId().equals(request.getInstituteId()))
                .count();
            log.info("Students with institute ID {}: {}", request.getInstituteId(), instituteCount);
        }
        
        // Convert to DTOs
        List<StudentResponseDto> studentDtos = page.getContent().stream()
                .map(details -> StudentResponseDto.fromEntity(details.getUser(), details))
                .toList();
        
        return StudentListResponseDto.success(
            studentDtos,
            studentDtos.size(),
            page.getNumber(),
            (int) page.getTotalElements()
        );
    }
    
    // Debug method to check if there are any students in the database
    public void debugStudentCount() {
        long totalStudents = studentDetailsRepository.count();
        log.info("Total students in database: {}", totalStudents);
        
        // Check students by role
        List<StudentDetails> allStudents = studentDetailsRepository.findAll();
        log.info("All students: {}", allStudents.size());
        
        for (StudentDetails student : allStudents) {
            log.info("Student: {} - Role: {} - InstituteId: {}", 
                student.getUser().getUsername(), 
                student.getUser().getRole(), 
                student.getUser().getInstituteId());
        }
        
        // Test specification with institute ID 3
        log.info("Testing specification with institute ID 3...");
        Specification<StudentDetails> spec = Specification.where(StudentSpecification.isStudent())
                .and(StudentSpecification.hasInstituteId(3L));
        
        List<StudentDetails> studentsWithSpec = studentDetailsRepository.findAll(spec);
        log.info("Students found with specification (instituteId=3): {}", studentsWithSpec.size());
        
        for (StudentDetails student : studentsWithSpec) {
            log.info("Specification result - Student: {} - Role: {} - InstituteId: {}", 
                student.getUser().getUsername(), 
                student.getUser().getRole(), 
                student.getUser().getInstituteId());
        }
    }
}
