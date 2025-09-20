package com.testpire.testpire.repository;

import com.testpire.testpire.entity.TeacherDetails;
import com.testpire.testpire.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherDetailsRepository extends JpaRepository<TeacherDetails, Long> {
    
    Optional<TeacherDetails> findByUser(User user);
    
    Optional<TeacherDetails> findByUserId(Long userId);
    
    @Query("SELECT td FROM TeacherDetails td WHERE td.user.instituteId = :instituteId")
    List<TeacherDetails> findByInstituteId(@Param("instituteId") Long instituteId);
    
    @Query("SELECT td FROM TeacherDetails td WHERE td.department = :department")
    List<TeacherDetails> findByDepartment(@Param("department") String department);
    
    @Query("SELECT td FROM TeacherDetails td WHERE td.subject = :subject")
    List<TeacherDetails> findBySubject(@Param("subject") String subject);
    
    @Query("SELECT td FROM TeacherDetails td WHERE " +
           "LOWER(td.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(td.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(td.department) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(td.subject) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<TeacherDetails> searchTeachers(@Param("query") String query);
    
    @Query("SELECT td FROM TeacherDetails td WHERE td.user.instituteId = :instituteId AND " +
           "(LOWER(td.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(td.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(td.department) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(td.subject) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<TeacherDetails> searchTeachersByInstitute(@Param("instituteId") Long instituteId, @Param("query") String query);
}
