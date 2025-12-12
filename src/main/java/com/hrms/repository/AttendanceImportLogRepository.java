package com.hrms.repository;

import com.hrms.entity.AttendanceImportLog;
import com.hrms.enums.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AttendanceImportLog entity.
 */
@Repository
public interface AttendanceImportLogRepository extends JpaRepository<AttendanceImportLog, Long> {

    /**
     * Find import logs for a company with pagination.
     */
    @Query("SELECT ail FROM AttendanceImportLog ail " +
           "WHERE ail.tenantId = :tenantId AND ail.company.id = :companyId " +
           "ORDER BY ail.createdAt DESC")
    Page<AttendanceImportLog> findByTenantAndCompany(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            Pageable pageable);

    /**
     * Find import logs with filters.
     * Uses native query to properly handle nullable enum parameter with PostgreSQL.
     */
    @Query(value = "SELECT * FROM attendance_import_log ail " +
           "WHERE ail.tenant_id = :tenantId AND ail.company_id = :companyId " +
           "AND (CAST(:status AS VARCHAR) IS NULL OR ail.status = :status) " +
           "AND (CAST(:fromDate AS TIMESTAMP WITH TIME ZONE) IS NULL OR ail.created_at >= :fromDate) " +
           "AND (CAST(:toDate AS TIMESTAMP WITH TIME ZONE) IS NULL OR ail.created_at <= :toDate) " +
           "ORDER BY ail.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM attendance_import_log ail " +
           "WHERE ail.tenant_id = :tenantId AND ail.company_id = :companyId " +
           "AND (CAST(:status AS VARCHAR) IS NULL OR ail.status = :status) " +
           "AND (CAST(:fromDate AS TIMESTAMP WITH TIME ZONE) IS NULL OR ail.created_at >= :fromDate) " +
           "AND (CAST(:toDate AS TIMESTAMP WITH TIME ZONE) IS NULL OR ail.created_at <= :toDate)",
           nativeQuery = true)
    Page<AttendanceImportLog> findWithFilters(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            Pageable pageable);

    /**
     * Count import logs with filters.
     */
    @Query("SELECT COUNT(ail) FROM AttendanceImportLog ail " +
           "WHERE ail.tenantId = :tenantId AND ail.company.id = :companyId " +
           "AND (:status IS NULL OR ail.status = :status) " +
           "AND (:fromDate IS NULL OR ail.createdAt >= :fromDate) " +
           "AND (:toDate IS NULL OR ail.createdAt <= :toDate)")
    long countWithFilters(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("status") ImportStatus status,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate);

    /**
     * Find by ID and tenant.
     */
    @Query("SELECT ail FROM AttendanceImportLog ail " +
           "LEFT JOIN FETCH ail.importedBy " +
           "WHERE ail.id = :id AND ail.tenantId = :tenantId")
    Optional<AttendanceImportLog> findByIdAndTenant(
            @Param("id") Long id,
            @Param("tenantId") String tenantId);

    /**
     * Find recent imports for a company.
     */
    @Query("SELECT ail FROM AttendanceImportLog ail " +
           "WHERE ail.tenantId = :tenantId AND ail.company.id = :companyId " +
           "ORDER BY ail.createdAt DESC")
    List<AttendanceImportLog> findRecentByTenantAndCompany(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            Pageable pageable);

    /**
     * Get import statistics for a company.
     */
    @Query("SELECT ail.status, COUNT(ail), SUM(ail.importedCount) " +
           "FROM AttendanceImportLog ail " +
           "WHERE ail.tenantId = :tenantId AND ail.company.id = :companyId " +
           "AND ail.createdAt >= :fromDate " +
           "GROUP BY ail.status")
    List<Object[]> getImportStatistics(
            @Param("tenantId") String tenantId,
            @Param("companyId") Long companyId,
            @Param("fromDate") OffsetDateTime fromDate);
}
