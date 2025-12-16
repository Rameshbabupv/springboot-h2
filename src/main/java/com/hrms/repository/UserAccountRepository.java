package com.hrms.repository;

import com.hrms.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserAccount entity
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Find by tenant
    List<UserAccount> findByTenantIdAndDeletedAtIsNull(String tenantId);

    // Find by username and tenant
    Optional<UserAccount> findByTenantIdAndUsernameAndDeletedAtIsNull(String tenantId, String username);

    // Find by email and tenant
    Optional<UserAccount> findByTenantIdAndEmailAndDeletedAtIsNull(String tenantId, String email);

    // Find by employee ID
    Optional<UserAccount> findByEmployeeIdAndDeletedAtIsNull(Long employeeId);

    // Find by role
    List<UserAccount> findByTenantIdAndRoleAndDeletedAtIsNull(String tenantId, String role);

    // Find active users
    List<UserAccount> findByTenantIdAndIsActiveTrueAndDeletedAtIsNull(String tenantId);

    // Find locked users
    List<UserAccount> findByTenantIdAndIsLockedTrueAndDeletedAtIsNull(String tenantId);

    // Check username exists
    boolean existsByTenantIdAndUsernameAndDeletedAtIsNull(String tenantId, String username);

    // Check email exists
    boolean existsByTenantIdAndEmailAndDeletedAtIsNull(String tenantId, String email);

    // Check employee already has account
    boolean existsByEmployeeIdAndDeletedAtIsNull(Long employeeId);

    // Find users with expired passwords
    @Query("SELECT u FROM UserAccount u WHERE u.tenantId = :tenantId " +
           "AND u.passwordExpiresAt IS NOT NULL " +
           "AND u.passwordExpiresAt < :currentDate " +
           "AND u.deletedAt IS NULL")
    List<UserAccount> findUsersWithExpiredPasswords(
        @Param("tenantId") String tenantId,
        @Param("currentDate") LocalDateTime currentDate
    );

    // Find users that need password change
    List<UserAccount> findByTenantIdAndMustChangePasswordTrueAndDeletedAtIsNull(String tenantId);

    // Count users by tenant
    long countByTenantIdAndDeletedAtIsNull(String tenantId);

    // Count active users by tenant
    long countByTenantIdAndIsActiveTrueAndDeletedAtIsNull(String tenantId);

    // =====================================================
    // KEYCLOAK AUTHENTICATION METHODS
    // =====================================================

    // Find by username (global search for login)
    Optional<UserAccount> findByUsername(String username);

    // Find by email (global search for signup validation)
    Optional<UserAccount> findByEmail(String email);

    // Check if email exists (global check)
    boolean existsByEmail(String email);

    // Check if username exists (global check)
    boolean existsByUsername(String username);

    // Find by Keycloak user ID
    Optional<UserAccount> findByKeycloakUserId(String keycloakUserId);

    // Find by Keycloak user ID (case-insensitive)
    Optional<UserAccount> findByKeycloakUserIdIgnoreCase(String keycloakUserId);
}
