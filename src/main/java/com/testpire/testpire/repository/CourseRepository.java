package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstituteId(Long instituteId);
    List<Course> findByInstituteIdAndActiveTrue(Long instituteId);
    List<Course> findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(Long instituteId1, String name, Long instituteId2, String code);
    Optional<Course> findByCodeAndInstituteId(String code, Long instituteId);
    boolean existsByCodeAndInstituteId(String code, Long instituteId);
    List<Course> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}

