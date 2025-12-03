package com.hrms.repository;

import com.hrms.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByTenantId(String tenantId);

    List<Grade> findByTenantIdAndIsActiveTrue(String tenantId);

    @Query("SELECT g FROM Grade g WHERE g.tenantId = :tenantId AND UPPER(g.code) = UPPER(:code)")
    Optional<Grade> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT g FROM Grade g WHERE g.tenantId = :tenantId AND LOWER(g.name) = LOWER(:name)")
    Optional<Grade> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    List<Grade> findByIsActiveTrue();

    @Query("SELECT g FROM Grade g WHERE g.tenantId = :tenantId AND g.isActive = true " +
           "AND (LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Grade> searchGrades(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
