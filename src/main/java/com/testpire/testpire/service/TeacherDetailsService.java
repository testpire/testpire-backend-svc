package com.testpire.testpire.service;

import com.testpire.testpire.entity.TeacherDetails;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.repository.TeacherDetailsRepository;
import com.testpire.testpire.util.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
}
