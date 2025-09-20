package com.testpire.testpire.repository;

import com.testpire.testpire.entity.StudentDetails;
import com.testpire.testpire.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDetailsRepository extends JpaRepository<StudentDetails, Long> {
    
    Optional<StudentDetails> findByUser(User user);
    
    Optional<StudentDetails> findByUserId(Long userId);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.user.instituteId = :instituteId")
    List<StudentDetails> findByInstituteId(@Param("instituteId") Long instituteId);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.course = :course")
    List<StudentDetails> findByCourse(@Param("course") String course);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.yearOfStudy = :yearOfStudy")
    List<StudentDetails> findByYearOfStudy(@Param("yearOfStudy") Integer yearOfStudy);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.user.instituteId = :instituteId AND sd.course = :course")
    List<StudentDetails> findByInstituteIdAndCourse(@Param("instituteId") Long instituteId, @Param("course") String course);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.user.instituteId = :instituteId AND sd.yearOfStudy = :yearOfStudy")
    List<StudentDetails> findByInstituteIdAndYearOfStudy(@Param("instituteId") Long instituteId, @Param("yearOfStudy") Integer yearOfStudy);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE " +
           "LOWER(sd.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.course) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.rollNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<StudentDetails> searchStudents(@Param("query") String query);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.user.instituteId = :instituteId AND " +
           "(LOWER(sd.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.course) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<StudentDetails> searchStudentsByInstitute(@Param("instituteId") Long instituteId, @Param("query") String query);
    
    @Query("SELECT sd FROM StudentDetails sd WHERE sd.user.instituteId = :instituteId AND sd.course = :course AND " +
           "(LOWER(sd.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(sd.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<StudentDetails> searchStudentsByInstituteAndCourse(@Param("instituteId") Long instituteId, @Param("course") String course, @Param("query") String query);
}
