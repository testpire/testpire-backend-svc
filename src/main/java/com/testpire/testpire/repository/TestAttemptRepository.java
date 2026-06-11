package com.testpire.testpire.repository;

import com.testpire.testpire.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    /** Bulk-delete every attempt (and, via DB cascade, its answers) for an institute. Used by institute teardown. */
    @Modifying
    @Query("DELETE FROM TestAttempt t WHERE t.instituteId = :instituteId")
    void deleteByInstituteId(@Param("instituteId") Long instituteId);

    List<TestAttempt> findByTestIdAndStudentUserId(Long testId, Long studentUserId);

    List<TestAttempt> findByStudentUserId(Long studentUserId);

    List<TestAttempt> findByTestId(Long testId);

    Optional<TestAttempt> findByIdAndStudentUserId(Long id, Long studentUserId);

    Optional<TestAttempt> findByIdAndInstituteId(Long id, Long instituteId);

    int countByTestIdAndStudentUserId(Long testId, Long studentUserId);
}
