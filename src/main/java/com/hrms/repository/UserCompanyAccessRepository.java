package com.hrms.repository;

import com.hrms.entity.UserCompanyAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserCompanyAccess entity.
 * Handles database operations for user-company relationships.
 */
@Repository
public interface UserCompanyAccessRepository extends JpaRepository<UserCompanyAccess, Long> {

    /**
     * Find all active companies for a specific user.
     *
     * @param userId User ID
     * @return List of UserCompanyAccess records
     */
    List<UserCompanyAccess> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find specific user-company access record.
     *
     * @param userId User ID
     * @param companyId Company ID
     * @return UserCompanyAccess if exists
     */
    Optional<UserCompanyAccess> findByUserIdAndCompanyId(Long userId, Long companyId);

    /**
     * Find all active company IDs for a user.
     *
     * @param userId User ID
     * @return List of company IDs
     */
    @Query("SELECT uca.companyId FROM UserCompanyAccess uca " +
           "WHERE uca.userId = :userId AND uca.isActive = true")
    List<Long> findActiveCompaniesByUserId(@Param("userId") Long userId);

    /**
     * Find all active users for a specific company.
     *
     * @param companyId Company ID
     * @return List of UserCompanyAccess records
     */
    List<UserCompanyAccess> findByCompanyIdAndIsActiveTrue(Long companyId);

    /**
     * Find all company access records for a user in specific tenant.
     *
     * @param userId User ID
     * @param tenantId Tenant ID
     * @return List of UserCompanyAccess records
     */
    List<UserCompanyAccess> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, String tenantId);

    /**
     * Check if user has access to a company.
     *
     * @param userId User ID
     * @param companyId Company ID
     * @return true if access exists and is active
     */
    boolean existsByUserIdAndCompanyIdAndIsActiveTrue(Long userId, Long companyId);
}
