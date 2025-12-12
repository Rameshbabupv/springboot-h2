package com.hrms.repository;

import com.hrms.entity.PunchLog;
import com.hrms.enums.PunchSource;
import com.hrms.enums.PunchStatus;
import com.hrms.enums.PunchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PunchLog entity.
 */
@Repository
public interface PunchLogRepository extends JpaRepository<PunchLog, Long> {

    /**
     * Find punches for an employee within a time range.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.employee.id = :employeeId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByEmployeeAndTimeRange(
            @Param("employeeId") Long employeeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find punches for a tenant within a time range.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByTenantAndTimeRange(
            @Param("tenantId") String tenantId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find punches by biometric ID.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.biometricId = :biometricId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByBiometricIdAndTimeRange(
            @Param("biometricId") String biometricId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find unmatched punches for processing.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
           "AND pl.status = 'UNMATCHED' " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findUnmatchedByTenantAndTimeRange(
            @Param("tenantId") String tenantId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find punches by source.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
           "AND pl.punchSource = :source " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByTenantAndSourceAndTimeRange(
            @Param("tenantId") String tenantId,
            @Param("source") PunchSource source,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find punches by type for an employee.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.employee.id = :employeeId " +
           "AND pl.punchType = :punchType " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByEmployeeAndTypeAndTimeRange(
            @Param("employeeId") Long employeeId,
            @Param("punchType") PunchType punchType,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Update punch status.
     */
    @Modifying
    @Query("UPDATE PunchLog pl SET pl.status = :status WHERE pl.id IN :ids")
    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") PunchStatus status);

    /**
     * Count punches for employee on a day.
     */
    @Query("SELECT COUNT(pl) FROM PunchLog pl WHERE pl.employee.id = :employeeId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime")
    long countByEmployeeAndTimeRange(
            @Param("employeeId") Long employeeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find first punch of day for employee.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.employee.id = :employeeId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime ASC LIMIT 1")
    PunchLog findFirstPunchOfDay(
            @Param("employeeId") Long employeeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find last punch of day for employee.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.employee.id = :employeeId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime DESC LIMIT 1")
    PunchLog findLastPunchOfDay(
            @Param("employeeId") Long employeeId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Find punches by device.
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.deviceId = :deviceId " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByDeviceAndTimeRange(
            @Param("deviceId") String deviceId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Check if duplicate punch exists.
     */
    @Query("SELECT COUNT(pl) > 0 FROM PunchLog pl WHERE pl.employee.id = :employeeId " +
           "AND pl.punchTime = :punchTime AND pl.punchSource = :source")
    boolean existsDuplicatePunch(
            @Param("employeeId") Long employeeId,
            @Param("punchTime") OffsetDateTime punchTime,
            @Param("source") PunchSource source);

    /**
     * Find punches by employee ID and time range (derived query).
     */
    List<PunchLog> findByEmployeeIdAndPunchTimeBetween(Long employeeId, OffsetDateTime start, OffsetDateTime end);

    /**
     * Count punches by employee ID and time range (derived query).
     */
    long countByEmployeeIdAndPunchTimeBetween(Long employeeId, OffsetDateTime start, OffsetDateTime end);

    // ========== ADR-002: SHA256-Based Deduplication Methods ==========

    /**
     * Check if punch with SHA256 hash already exists (PRIMARY deduplication per ADR-002).
     * O(1) lookup via unique index.
     * 
     * @param sha256 SHA256 hash
     * @return true if exists, false otherwise
     * @since 2.0 (ADR-002)
     */
    boolean existsBySha256(String sha256);

    /**
     * Find punch by SHA256 hash.
     * Used for diagnostics and debugging.
     *
     * @param sha256 SHA256 hash
     * @return Optional of PunchLog
     * @since 2.0 (ADR-002)
     */
    Optional<PunchLog> findBySha256(String sha256);

    /**
     * Find all punches from a specific source within time range.
     * Used for source-specific analytics.
     *
     * @param tenantId Tenant ID
     * @param source Punch source
     * @param startTime Start of range
     * @param endTime End of range
     * @return List of punches
     * @since 2.0 (ADR-002)
     */
    @Query("SELECT pl FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
           "AND pl.punchSource = :source " +
           "AND pl.punchTime BETWEEN :startTime AND :endTime " +
           "ORDER BY pl.punchTime")
    List<PunchLog> findByTenantAndSourceAndTime(
            @Param("tenantId") String tenantId,
            @Param("source") PunchSource source,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    /**
     * Count punches by source for monitoring.
     *
     * @param tenantId Tenant ID
     * @param source Punch source
     * @return Count
     * @since 2.0 (ADR-002)
     */
    @Query("SELECT COUNT(pl) FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
           "AND pl.punchSource = :source")
    long countByTenantAndSource(
            @Param("tenantId") String tenantId,
            @Param("source") PunchSource source);
}
