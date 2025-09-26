package com.testpire.testpire.repository;

import com.testpire.testpire.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByInstituteId(Long instituteId);
    List<Topic> findByInstituteIdAndActiveTrue(Long instituteId);
    List<Topic> findByChapterIdAndInstituteId(Long chapterId, Long instituteId);
    List<Topic> findByChapterIdAndInstituteIdAndActiveTrue(Long chapterId, Long instituteId);
    List<Topic> findByInstituteIdAndNameContainingIgnoreCaseOrInstituteIdAndCodeContainingIgnoreCase(Long instituteId1, String name, Long instituteId2, String code);
    Optional<Topic> findByCodeAndInstituteId(String code, Long instituteId);
    boolean existsByCodeAndInstituteId(String code, Long instituteId);
    List<Topic> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
    List<Topic> findByChapterIdOrderByOrderIndex(Long chapterId);
    
    // Advanced search with multiple filters
    @Query("SELECT t FROM Topic t WHERE t.instituteId = :instituteId " +
        "AND (:courseId IS NULL OR t.chapter.subject.course.id = :courseId) " +
        "AND (:subjectId IS NULL OR t.chapter.subject.id = :subjectId) " +
        "AND (:chapterId IS NULL OR t.chapter.id = :chapterId) " +
        "AND (:active IS NULL OR COALESCE(t.active, true) = :active) " +
        "AND (:searchQuery IS NULL OR " +
        "     LOWER(t.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
        "     LOWER(t.code) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
        "     LOWER(t.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
        "ORDER BY t.chapter.subject.course.name, t.chapter.subject.name, t.chapter.name, t.orderIndex")
    List<Topic> findTopicsWithFilters(@Param("instituteId") Long instituteId,
        @Param("courseId") Long courseId,
        @Param("subjectId") Long subjectId,
        @Param("chapterId") Long chapterId,
        @Param("searchQuery") String searchQuery,
        @Param("active") Boolean active);
    
    // Count topics with filters
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.instituteId = :instituteId " +
           "AND (:courseId IS NULL OR t.chapter.subject.course.id = :courseId) " +
           "AND (:subjectId IS NULL OR t.chapter.subject.id = :subjectId) " +
           "AND (:chapterId IS NULL OR t.chapter.id = :chapterId) " +
           "AND (:active IS NULL OR t.active = :active) " +
           "AND (:searchQuery IS NULL OR " +
           "     LOWER(t.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "     LOWER(t.code) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "     LOWER(t.description) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Long countTopicsWithFilters(@Param("instituteId") Long instituteId,
                              @Param("courseId") Long courseId,
                              @Param("subjectId") Long subjectId,
                              @Param("chapterId") Long chapterId,
                              @Param("searchQuery") String searchQuery,
                              @Param("active") Boolean active);
}
