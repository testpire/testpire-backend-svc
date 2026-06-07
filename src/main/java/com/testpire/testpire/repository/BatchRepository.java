package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long>, JpaSpecificationExecutor<Batch> {

    Optional<Batch> findByIdAndInstituteId(Long id, Long instituteId);

    List<Batch> findByCourseIdAndInstituteId(Long courseId, Long instituteId);

    List<Batch> findByCourseId(Long courseId);

    boolean existsByCourseIdAndNameIgnoreCase(Long courseId, String name);

    boolean existsByCourseIdAndCode(Long courseId, String code);
}
