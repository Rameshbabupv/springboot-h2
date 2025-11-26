package com.hrms.repository;

import com.hrms.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByTenantId(String tenantId);

    List<Section> findByTenantIdAndIsActiveTrue(String tenantId);

    List<Section> findByDepartmentId(Long departmentId);

    List<Section> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    List<Section> findByIsActiveTrue();

    // Department-scoped case-insensitive duplicate checks
    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.department.id = :departmentId AND UPPER(s.code) = UPPER(:code)")
    Optional<Section> findByTenantIdAndDepartmentIdAndCodeIgnoreCase(
        @Param("tenantId") String tenantId,
        @Param("departmentId") Long departmentId,
        @Param("code") String code
    );

    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.department.id = :departmentId AND LOWER(s.name) = LOWER(:name)")
    Optional<Section> findByTenantIdAndDepartmentIdAndNameIgnoreCase(
        @Param("tenantId") String tenantId,
        @Param("departmentId") Long departmentId,
        @Param("name") String name
    );

    // Search functionality including department name
    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.isActive = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.department.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Section> searchSections(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);

    // Search within a specific department
    @Query("SELECT s FROM Section s WHERE s.tenantId = :tenantId AND s.department.id = :departmentId AND s.isActive = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Section> searchSectionsByDepartment(
        @Param("tenantId") String tenantId,
        @Param("departmentId") Long departmentId,
        @Param("searchTerm") String searchTerm
    );
}
