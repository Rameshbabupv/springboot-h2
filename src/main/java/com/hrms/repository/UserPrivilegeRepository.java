package com.hrms.repository;

import com.hrms.entity.UserPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserPrivilege entity
 */
@Repository
public interface UserPrivilegeRepository extends JpaRepository<UserPrivilege, Long> {

    // Find all privileges for a user
    List<UserPrivilege> findByTenantIdAndUserId(String tenantId, Long userId);

    // Find specific privilege for user and module
    Optional<UserPrivilege> findByTenantIdAndUserIdAndModuleCode(
        String tenantId, Long userId, String moduleCode);

    // Delete all privileges for a user
    @Modifying
    @Transactional
    void deleteByTenantIdAndUserId(String tenantId, Long userId);

    // Count privileges for a user
    long countByTenantIdAndUserId(String tenantId, Long userId);

    // Find privileges by module code
    List<UserPrivilege> findByTenantIdAndModuleCode(String tenantId, String moduleCode);

    // Check if user has any permission for a module
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM UserPrivilege p " +
           "WHERE p.tenantId = :tenantId AND p.userId = :userId AND p.moduleCode = :moduleCode " +
           "AND (p.canView = true OR p.canAdd = true OR p.canEdit = true OR p.canDelete = true)")
    boolean hasAnyPermission(@Param("tenantId") String tenantId,
                            @Param("userId") Long userId,
                            @Param("moduleCode") String moduleCode);
}
