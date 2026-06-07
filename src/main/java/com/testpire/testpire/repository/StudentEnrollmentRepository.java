package com.testpire.testpire.repository;

import com.testpire.testpire.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {

    List<StudentEnrollment> findByStudentUserId(Long studentUserId);

    /** Bulk variant of {@link #findByStudentUserId} — one query for many students (avoids N+1 in list views). */
    List<StudentEnrollment> findByStudentUserIdIn(Collection<Long> studentUserIds);

    List<StudentEnrollment> findByCourseId(Long courseId);

    List<StudentEnrollment> findByBatchId(Long batchId);

    boolean existsByStudentUserIdAndCourseId(Long studentUserId, Long courseId);

    void deleteByStudentUserId(Long studentUserId);
}
