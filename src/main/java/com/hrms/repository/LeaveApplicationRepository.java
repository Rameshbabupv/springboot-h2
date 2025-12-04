package com.hrms.repository;

import com.hrms.entity.LeaveApplication;
import com.hrms.enums.LeaveApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LeaveApplication entity.
 * Transactional table - always filters by company_id.
 */
@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    /**
     * Find applications by employee.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "LEFT JOIN FETCH la.leaveType " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.employee.id = :employeeId " +
           "AND la.isDeleted = false " +
           "ORDER BY la.fromDate DESC")
    List<LeaveApplication> findByEmployee(@Param("tenantId") String tenantId,
                                           @Param("companyId") Long companyId,
                                           @Param("employeeId") Long employeeId);

    /**
     * Find applications by employee and status.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "LEFT JOIN FETCH la.leaveType " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.employee.id = :employeeId " +
           "AND la.status = :status " +
           "AND la.isDeleted = false " +
           "ORDER BY la.fromDate DESC")
    List<LeaveApplication> findByEmployeeAndStatus(@Param("tenantId") String tenantId,
                                                    @Param("companyId") Long companyId,
                                                    @Param("employeeId") Long employeeId,
                                                    @Param("status") LeaveApplicationStatus status);

    /**
     * Find applications by company and date range.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "LEFT JOIN FETCH la.employee " +
           "LEFT JOIN FETCH la.leaveType " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.fromDate <= :toDate AND la.toDate >= :fromDate " +
           "AND la.isDeleted = false " +
           "ORDER BY la.fromDate DESC")
    List<LeaveApplication> findByCompanyAndDateRange(@Param("tenantId") String tenantId,
                                                      @Param("companyId") Long companyId,
                                                      @Param("fromDate") LocalDate fromDate,
                                                      @Param("toDate") LocalDate toDate);

    /**
     * Find pending applications for approval (by reporting manager).
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "LEFT JOIN FETCH la.employee e " +
           "LEFT JOIN FETCH la.leaveType " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.status = 'PENDING' " +
           "AND e.reportingManager.id = :managerId " +
           "AND la.isDeleted = false " +
           "ORDER BY la.appliedAt ASC")
    List<LeaveApplication> findPendingForApprover(@Param("tenantId") String tenantId,
                                                   @Param("companyId") Long companyId,
                                                   @Param("managerId") Long managerId);

    /**
     * Find applications by status.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "LEFT JOIN FETCH la.employee " +
           "LEFT JOIN FETCH la.leaveType " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.status = :status " +
           "AND la.isDeleted = false " +
           "ORDER BY la.appliedAt DESC")
    List<LeaveApplication> findByCompanyAndStatus(@Param("tenantId") String tenantId,
                                                   @Param("companyId") Long companyId,
                                                   @Param("status") LeaveApplicationStatus status);

    /**
     * Find application with all details.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "LEFT JOIN FETCH la.employee " +
           "LEFT JOIN FETCH la.leaveType " +
           "LEFT JOIN FETCH la.leavePolicy " +
           "LEFT JOIN FETCH la.attachments " +
           "WHERE la.id = :id AND la.isDeleted = false")
    Optional<LeaveApplication> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find by application number.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.applicationNumber = :applicationNumber " +
           "AND la.isDeleted = false")
    Optional<LeaveApplication> findByApplicationNumber(@Param("tenantId") String tenantId,
                                                        @Param("companyId") Long companyId,
                                                        @Param("applicationNumber") String applicationNumber);

    /**
     * Check for overlapping applications.
     */
    @Query("SELECT COUNT(la) > 0 FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.employee.id = :employeeId " +
           "AND la.status NOT IN ('REJECTED', 'CANCELLED', 'WITHDRAWN') " +
           "AND la.fromDate <= :toDate AND la.toDate >= :fromDate " +
           "AND la.id != :excludeId " +
           "AND la.isDeleted = false")
    boolean hasOverlappingApplication(@Param("tenantId") String tenantId,
                                       @Param("employeeId") Long employeeId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate,
                                       @Param("excludeId") Long excludeId);

    /**
     * Get next application number sequence.
     */
    @Query("SELECT COUNT(la) + 1 FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.applicationNumber LIKE :prefix%")
    Long getNextSequence(@Param("tenantId") String tenantId,
                          @Param("companyId") Long companyId,
                          @Param("prefix") String prefix);

    /**
     * Find applications with pagination.
     */
    @Query("SELECT la FROM LeaveApplication la " +
           "WHERE la.tenantId = :tenantId " +
           "AND la.company.id = :companyId " +
           "AND la.isDeleted = false")
    Page<LeaveApplication> findByCompany(@Param("tenantId") String tenantId,
                                          @Param("companyId") Long companyId,
                                          Pageable pageable);
}
