package com.testpire.testpire.repository;

import com.testpire.testpire.entity.TestAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptAnswerRepository extends JpaRepository<TestAttemptAnswer, Long> {

    List<TestAttemptAnswer> findByAttemptId(Long attemptId);

    Optional<TestAttemptAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    boolean existsByQuestionId(Long questionId);
}
