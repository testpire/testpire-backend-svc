package com.testpire.testpire.repository;

import com.testpire.testpire.entity.TestAssignment;
import com.testpire.testpire.enums.AssignmentTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestAssignmentRepository extends JpaRepository<TestAssignment, Long> {

    List<TestAssignment> findByTestId(Long testId);

    Optional<TestAssignment> findByIdAndInstituteId(Long id, Long instituteId);

    boolean existsByTestIdAndTargetTypeAndTargetId(Long testId, AssignmentTargetType targetType, Long targetId);

    /**
     * Assignments in an institute matching any of the given (type, id) targets — the core of
     * dynamic resolution. The caller passes the student's course ids, batch ids, and own user id as
     * separate target sets; an empty set for a type simply matches nothing.
     */
    @Query("""
            SELECT a FROM TestAssignment a
            WHERE a.instituteId = :instituteId AND (
                (a.targetType = com.testpire.testpire.enums.AssignmentTargetType.COURSE  AND a.targetId IN :courseIds) OR
                (a.targetType = com.testpire.testpire.enums.AssignmentTargetType.BATCH   AND a.targetId IN :batchIds) OR
                (a.targetType = com.testpire.testpire.enums.AssignmentTargetType.STUDENT AND a.targetId = :studentUserId)
            )
            """)
    List<TestAssignment> findForStudentTargets(@Param("instituteId") Long instituteId,
                                               @Param("courseIds") Collection<Long> courseIds,
                                               @Param("batchIds") Collection<Long> batchIds,
                                               @Param("studentUserId") Long studentUserId);
}
