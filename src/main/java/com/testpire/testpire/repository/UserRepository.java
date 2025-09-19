package com.testpire.testpire.repository;

import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByCognitoUserId(String cognitoUserId);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByInstituteId(Long instituteId);
    
    List<User> findByRoleAndInstituteId(UserRole role, Long instituteId);
    
    List<User> findByEnabledTrue();
    
    List<User> findByRoleAndEnabledTrue(UserRole role);
    
    List<User> findByInstituteIdAndEnabledTrue(Long instituteId);
    
    List<User> findByRoleAndInstituteIdAndEnabledTrue(UserRole role, Long instituteId);
    
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.role = :role AND (u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm% OR u.email LIKE %:searchTerm%)")
    List<User> findByRoleAndEnabledTrueAndNameOrEmailContaining(
        @Param("role") UserRole role, 
        @Param("searchTerm") String searchTerm
    );
    
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.role = :role AND u.instituteId = :instituteId AND (u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm% OR u.email LIKE %:searchTerm%)")
    List<User> findByRoleAndInstituteIdAndEnabledTrueAndNameOrEmailContaining(
        @Param("role") UserRole role, 
        @Param("instituteId") Long instituteId,
        @Param("searchTerm") String searchTerm
    );
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByCognitoUserId(String cognitoUserId);
} 