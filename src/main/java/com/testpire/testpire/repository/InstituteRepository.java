package com.testpire.testpire.repository;

import com.testpire.testpire.mongoDomain.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    
    Optional<Institute> findByCode(String code);
    
    Optional<Institute> findByCodeAndActiveTrue(String code);
    
    List<Institute> findByActiveTrue();
    
    @Query("SELECT i FROM Institute i WHERE i.active = true AND (i.name LIKE %:searchTerm% OR i.code LIKE %:searchTerm%)")
    List<Institute> findByActiveTrueAndNameOrCodeContaining(@Param("searchTerm") String searchTerm);
    
    boolean existsByCode(String code);
    
    boolean existsByEmail(String email);
} 