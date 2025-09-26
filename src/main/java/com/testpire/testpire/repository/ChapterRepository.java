package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByInstituteId(Long instituteId);
    List<Chapter> findByInstituteIdAndActiveTrue(Long instituteId);
    List<Chapter> findBySubjectIdAndInstituteId(Long subjectId, Long instituteId);
    List<Chapter> findBySubjectIdAndInstituteIdAndActiveTrue(Long subjectId, Long instituteId);
    List<Chapter> findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(Long instituteId1, String name, Long instituteId2, String code);
    Optional<Chapter> findByCodeAndInstituteId(String code, Long instituteId);
    boolean existsByCodeAndInstituteId(String code, Long instituteId);
    List<Chapter> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
    List<Chapter> findBySubjectIdOrderByOrderIndex(Long subjectId);
}

