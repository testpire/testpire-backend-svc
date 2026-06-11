package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends JpaRepository<Test, Long>, JpaSpecificationExecutor<Test> {

    Optional<Test> findByIdAndInstituteId(Long id, Long instituteId);

    List<Test> findByInstituteId(Long instituteId);

    /** Bulk-delete every test (and, via DB cascade, its test_questions) for an institute. Used by institute teardown. */
    @Modifying
    @Query("DELETE FROM Test t WHERE t.instituteId = :instituteId")
    void deleteByInstituteId(@Param("instituteId") Long instituteId);
}
