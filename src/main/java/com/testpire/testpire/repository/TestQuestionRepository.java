package com.testpire.testpire.repository;

import com.testpire.testpire.entity.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {

    List<TestQuestion> findByTestIdOrderBySortOrderAsc(Long testId);

    Optional<TestQuestion> findByTestIdAndQuestionId(Long testId, Long questionId);

    boolean existsByTestIdAndQuestionId(Long testId, Long questionId);
}
