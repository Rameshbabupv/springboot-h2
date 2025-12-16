package com.hrms.repository;

import com.hrms.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Route entity.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Find all routes for a specific tenant.
     */
    List<Route> findByTenantIdAndIsActiveTrue(String tenantId);

    /**
     * Find route by tenant and ID.
     */
    Optional<Route> findByIdAndTenantId(Long id, String tenantId);

    /**
     * Find route by tenant and code.
     */
    Optional<Route> findByTenantIdAndCode(String tenantId, String code);

    /**
     * Find route by tenant and name.
     */
    Optional<Route> findByTenantIdAndName(String tenantId, String name);

    /**
     * Check if route exists with given name (excluding specific ID).
     */
    boolean existsByTenantIdAndNameAndIdNot(String tenantId, String name, Long id);

    /**
     * Check if route exists with given code (excluding specific ID).
     */
    boolean existsByTenantIdAndCodeAndIdNot(String tenantId, String code, Long id);
}
