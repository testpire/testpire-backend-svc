package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long>, JpaSpecificationExecutor<Option> {
    
    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId ORDER BY o.optionOrder")
    List<Option> findByQuestionIdOrderByOptionOrder(@Param("questionId") Long questionId);

    @Query("SELECT o FROM Option o WHERE o.id = :id AND o.question.id = :questionId")
    Optional<Option> findByIdAndQuestionId(@Param("id") Long id, @Param("questionId") Long questionId);

    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId AND o.isCorrect = true")
    List<Option> findCorrectOptionsByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(o) FROM Option o WHERE o.question.id = :questionId")
    Long countByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(o) FROM Option o WHERE o.question.id = :questionId AND o.isCorrect = true")
    Long countCorrectOptionsByQuestionId(@Param("questionId") Long questionId);
}
