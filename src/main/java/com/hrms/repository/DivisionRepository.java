package com.hrms.repository;

import com.hrms.entity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {

    List<Division> findByTenantId(String tenantId);

    List<Division> findByTenantIdAndIsActiveTrue(String tenantId);

    Optional<Division> findByTenantIdAndCode(String tenantId, String code);

    List<Division> findByIsActiveTrue();

    // Case-insensitive duplicate checks
    @Query("SELECT d FROM Division d WHERE d.tenantId = :tenantId AND UPPER(d.code) = UPPER(:code)")
    Optional<Division> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT d FROM Division d WHERE d.tenantId = :tenantId AND LOWER(d.name) = LOWER(:name)")
    Optional<Division> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    // Search functionality
    @Query("SELECT d FROM Division d WHERE d.tenantId = :tenantId AND d.isActive = true " +
           "AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Division> searchDivisions(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
