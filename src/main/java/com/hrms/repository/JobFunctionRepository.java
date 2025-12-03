package com.hrms.repository;

import com.hrms.entity.JobFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobFunctionRepository extends JpaRepository<JobFunction, Long> {

    List<JobFunction> findByTenantId(String tenantId);

    List<JobFunction> findByTenantIdAndIsActiveTrue(String tenantId);

    List<JobFunction> findByIsActiveTrue();

    List<JobFunction> findByTenantIdAndFunctionGroup(String tenantId, String functionGroup);

    // Case-insensitive duplicate checks
    @Query("SELECT j FROM JobFunction j WHERE j.tenantId = :tenantId AND UPPER(j.code) = UPPER(:code)")
    Optional<JobFunction> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT j FROM JobFunction j WHERE j.tenantId = :tenantId AND LOWER(j.name) = LOWER(:name)")
    Optional<JobFunction> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    // Search functionality
    @Query("SELECT j FROM JobFunction j WHERE j.tenantId = :tenantId AND j.isActive = true " +
           "AND (LOWER(j.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(j.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(j.functionGroup) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<JobFunction> searchJobFunctions(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
