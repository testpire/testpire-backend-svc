package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByInstituteId(Long instituteId);
    List<Subject> findByInstituteIdAndActiveTrue(Long instituteId);
    List<Subject> findByCourseIdAndInstituteId(Long courseId, Long instituteId);
    List<Subject> findByCourseIdAndInstituteIdAndActiveTrue(Long courseId, Long instituteId);
    List<Subject> findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(Long instituteId1, String name, Long instituteId2, String code);
    Optional<Subject> findByCodeAndInstituteId(String code, Long instituteId);
    boolean existsByCodeAndInstituteId(String code, Long instituteId);
    List<Subject> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}

