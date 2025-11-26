package com.hrms.repository;

import com.hrms.entity.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {

    List<EmploymentType> findByTenantId(String tenantId);

    List<EmploymentType> findByTenantIdAndIsActiveTrue(String tenantId);

    @Query("SELECT e FROM EmploymentType e WHERE e.tenantId = :tenantId AND UPPER(e.code) = UPPER(:code)")
    Optional<EmploymentType> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT e FROM EmploymentType e WHERE e.tenantId = :tenantId AND LOWER(e.name) = LOWER(:name)")
    Optional<EmploymentType> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    List<EmploymentType> findByIsActiveTrue();

    @Query("SELECT e FROM EmploymentType e WHERE e.tenantId = :tenantId AND e.isActive = true " +
           "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<EmploymentType> searchEmploymentTypes(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
