package com.hrms.repository;

import com.hrms.entity.Shift;
import com.hrms.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Shift entity.
 * Supports shared data pattern: company_id NULL = tenant-wide.
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /**
     * Find all shifts for a company (includes shared + company-specific).
     */
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId " +
           "AND (s.company IS NULL OR s.company.id = :companyId) " +
           "ORDER BY s.displayOrder, s.code")
    List<Shift> findByTenantAndCompany(@Param("tenantId") String tenantId,
                                        @Param("companyId") Long companyId);

    /**
     * Find active shifts for a company.
     */
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId " +
           "AND (s.company IS NULL OR s.company.id = :companyId) " +
           "AND s.isActive = true " +
           "ORDER BY s.displayOrder, s.code")
    List<Shift> findActiveByTenantAndCompany(@Param("tenantId") String tenantId,
                                              @Param("companyId") Long companyId);

    /**
     * Find shift by code.
     */
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId AND s.code = :code")
    Optional<Shift> findByTenantAndCode(@Param("tenantId") String tenantId,
                                         @Param("code") String code);

    /**
     * Find shift with breaks eagerly loaded.
     */
    @Query("SELECT s FROM Shift s LEFT JOIN FETCH s.breaks WHERE s.id = :id")
    Optional<Shift> findByIdWithBreaks(@Param("id") Long id);

    /**
     * Find shifts by type.
     */
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId " +
           "AND (s.company IS NULL OR s.company.id = :companyId) " +
           "AND s.shiftType = :shiftType AND s.isActive = true")
    List<Shift> findByTenantAndCompanyAndType(@Param("tenantId") String tenantId,
                                               @Param("companyId") Long companyId,
                                               @Param("shiftType") ShiftType shiftType);

    /**
     * Find night shifts.
     */
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId " +
           "AND (s.company IS NULL OR s.company.id = :companyId) " +
           "AND s.isNightShift = true AND s.isActive = true")
    List<Shift> findNightShifts(@Param("tenantId") String tenantId,
                                 @Param("companyId") Long companyId);

    /**
     * Find OT eligible shifts.
     */
    @Query("SELECT s FROM Shift s WHERE s.tenantId = :tenantId " +
           "AND (s.company IS NULL OR s.company.id = :companyId) " +
           "AND s.isOtEligible = true AND s.isActive = true")
    List<Shift> findOtEligibleShifts(@Param("tenantId") String tenantId,
                                      @Param("companyId") Long companyId);

    /**
     * Check if code exists.
     */
    @Query("SELECT COUNT(s) > 0 FROM Shift s WHERE s.tenantId = :tenantId AND s.code = :code")
    boolean existsByTenantAndCode(@Param("tenantId") String tenantId,
                                   @Param("code") String code);

    /**
     * Find all shifts for tenant (admin view).
     */
    List<Shift> findByTenantIdOrderByDisplayOrderAscCodeAsc(String tenantId);
}
