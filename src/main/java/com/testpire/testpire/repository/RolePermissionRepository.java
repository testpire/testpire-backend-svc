package com.testpire.testpire.repository;

import com.testpire.testpire.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * Bulk-deletes all grants for a role as a direct DELETE statement (not load-then-remove), so it
     * executes immediately and the subsequent re-inserts in the same transaction don't collide with
     * the stale rows on the {@code (role, permission_code)} unique constraint.
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role = :role")
    void deleteByRole(@Param("role") String role);
}
