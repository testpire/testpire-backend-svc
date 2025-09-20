package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByInstituteId(Long instituteId);
    List<Topic> findByInstituteIdAndActiveTrue(Long instituteId);
    List<Topic> findByChapterIdAndInstituteId(Long chapterId, Long instituteId);
    List<Topic> findByChapterIdAndInstituteIdAndActiveTrue(Long chapterId, Long instituteId);
    List<Topic> findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(Long instituteId1, String name, Long instituteId2, String code);
    Optional<Topic> findByCodeAndInstituteId(String code, Long instituteId);
    boolean existsByCodeAndInstituteId(String code, Long instituteId);
    List<Topic> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
    List<Topic> findByChapterIdOrderByOrderIndex(Long chapterId);
}
