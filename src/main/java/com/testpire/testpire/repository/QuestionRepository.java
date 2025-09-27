package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Question;
import com.testpire.testpire.enums.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    
    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId AND q.active = true AND q.deleted = false")
    List<Question> findByTopicIdAndInstituteIdAndActiveTrueAndDeletedFalse(@Param("topicId") Long topicId, @Param("instituteId") Long instituteId);
    
    List<Question> findByInstituteIdAndActiveTrueAndDeletedFalse(Long instituteId);
    
    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId AND q.difficultyLevel = :difficultyLevel AND q.active = true AND q.deleted = false")
    List<Question> findByTopicIdAndInstituteIdAndDifficultyLevelAndActiveTrueAndDeletedFalse(
            @Param("topicId") Long topicId, @Param("instituteId") Long instituteId, @Param("difficultyLevel") DifficultyLevel difficultyLevel);
    
    
    Optional<Question> findByIdAndInstituteIdAndActiveTrueAndDeletedFalse(Long id, Long instituteId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.instituteId = :instituteId AND q.active = true AND q.deleted = false")
    Long countByInstituteId(@Param("instituteId") Long instituteId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId AND q.active = true AND q.deleted = false")
    Long countByTopicIdAndInstituteId(@Param("topicId") Long topicId, @Param("instituteId") Long instituteId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId AND q.difficultyLevel = :difficultyLevel AND q.active = true AND q.deleted = false")
    Long countByTopicIdAndInstituteIdAndDifficultyLevel(@Param("topicId") Long topicId, @Param("instituteId") Long instituteId, @Param("difficultyLevel") DifficultyLevel difficultyLevel);
}
