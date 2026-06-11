package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByIdAndInstituteId(Long id, Long instituteId);

    /** Bulk-delete every lead for an institute. Used by institute teardown (leads.institute_id has no cascade). */
    @Modifying
    @Query("DELETE FROM Lead l WHERE l.instituteId = :instituteId")
    void deleteByInstituteId(@Param("instituteId") Long instituteId);
}
