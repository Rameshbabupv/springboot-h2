package com.hrms.repository;

import com.hrms.entity.UserOrganizationalScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for UserOrganizationalScope entity
 */
@Repository
public interface UserOrganizationalScopeRepository extends JpaRepository<UserOrganizationalScope, Long> {

    // Find all scopes for a user
    List<UserOrganizationalScope> findByTenantIdAndUserId(String tenantId, Long userId);

    // Find scopes by user and scope type
    List<UserOrganizationalScope> findByTenantIdAndUserIdAndScopeType(
        String tenantId, Long userId, String scopeType);

    // Find scopes by scope type
    List<UserOrganizationalScope> findByTenantIdAndScopeType(String tenantId, String scopeType);

    // Find scopes by scope type and scope ID
    List<UserOrganizationalScope> findByTenantIdAndScopeTypeAndScopeId(
        String tenantId, String scopeType, Long scopeId);

    // Delete all scopes for a user
    @Modifying
    @Transactional
    void deleteByTenantIdAndUserId(String tenantId, Long userId);

    // Delete specific scope type for a user
    @Modifying
    @Transactional
    void deleteByTenantIdAndUserIdAndScopeType(String tenantId, Long userId, String scopeType);

    // Count scopes for a user
    long countByTenantIdAndUserId(String tenantId, Long userId);

    // Count scopes for a user by scope type
    long countByTenantIdAndUserIdAndScopeType(String tenantId, Long userId, String scopeType);
}
