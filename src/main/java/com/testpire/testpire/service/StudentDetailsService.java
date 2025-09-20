package com.testpire.testpire.service;

import com.testpire.testpire.entity.StudentDetails;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.repository.StudentDetailsRepository;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
