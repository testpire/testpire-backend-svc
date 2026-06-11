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
    
    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId")
    List<Question> findByTopicIdAndInstituteId(@Param("topicId") Long topicId, @Param("instituteId") Long instituteId);

    List<Question> findByInstituteId(Long instituteId);

    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId AND q.difficultyLevel = :difficultyLevel")
    List<Question> findByTopicIdAndInstituteIdAndDifficultyLevel(
            @Param("topicId") Long topicId, @Param("instituteId") Long instituteId, @Param("difficultyLevel") DifficultyLevel difficultyLevel);


    Optional<Question> findByIdAndInstituteId(Long id, Long instituteId);

    Optional<Question> findByInstituteIdAndExternalId(Long instituteId, String externalId);

    @Query("SELECT q FROM Question q WHERE q.topic.id = :topicId")
    List<Question> findByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.instituteId = :instituteId")
    Long countByInstituteId(@Param("instituteId") Long instituteId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId")
    Long countByTopicIdAndInstituteId(@Param("topicId") Long topicId, @Param("instituteId") Long instituteId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.topic.id = :topicId AND q.instituteId = :instituteId AND q.difficultyLevel = :difficultyLevel")
    Long countByTopicIdAndInstituteIdAndDifficultyLevel(@Param("topicId") Long topicId, @Param("instituteId") Long instituteId, @Param("difficultyLevel") DifficultyLevel difficultyLevel);
}
