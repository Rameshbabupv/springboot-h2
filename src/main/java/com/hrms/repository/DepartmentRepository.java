package com.hrms.repository;

import com.hrms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByTenantId(String tenantId);

    List<Department> findByTenantIdAndIsActiveTrue(String tenantId);

    Optional<Department> findByTenantIdAndCode(String tenantId, String code);

    List<Department> findByIsActiveTrue();

    // Case-insensitive duplicate checks
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND UPPER(d.code) = UPPER(:code)")
    Optional<Department> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND LOWER(d.name) = LOWER(:name)")
    Optional<Department> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    // Search functionality
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND d.isActive = true " +
           "AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Department> searchDepartments(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
