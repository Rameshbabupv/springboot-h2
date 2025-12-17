package com.hrms.repository;

import com.hrms.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    List<Designation> findByTenantId(String tenantId);

    List<Designation> findByTenantIdAndIsActiveTrue(String tenantId);

    List<Designation> findByIsActiveTrue();

    // Case-insensitive duplicate checks
    @Query("SELECT d FROM Designation d WHERE d.tenantId = :tenantId AND UPPER(d.code) = UPPER(:code)")
    Optional<Designation> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT d FROM Designation d WHERE d.tenantId = :tenantId AND LOWER(d.name) = LOWER(:name)")
    Optional<Designation> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    // Search functionality
    @Query("SELECT d FROM Designation d WHERE d.tenantId = :tenantId AND d.isActive = true " +
           "AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Designation> searchDesignations(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);

    // Scope-aware query
    List<Designation> findByTenantIdAndIdInAndIsActiveTrue(String tenantId, List<Long> ids);
}
