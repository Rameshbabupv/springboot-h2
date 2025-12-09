package com.hrms.repository;

import com.hrms.entity.AttendanceImportError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AttendanceImportError entity.
 */
@Repository
public interface AttendanceImportErrorRepository extends JpaRepository<AttendanceImportError, Long> {

    /**
     * Find errors for an import log with pagination.
     */
    @Query("SELECT aie FROM AttendanceImportError aie " +
           "WHERE aie.importLog.id = :importLogId " +
           "ORDER BY aie.rowNumber")
    Page<AttendanceImportError> findByImportLogId(
            @Param("importLogId") Long importLogId,
            Pageable pageable);

    /**
     * Find all errors for an import log.
     */
    @Query("SELECT aie FROM AttendanceImportError aie " +
           "WHERE aie.importLog.id = :importLogId " +
           "ORDER BY aie.rowNumber")
    List<AttendanceImportError> findAllByImportLogId(@Param("importLogId") Long importLogId);

    /**
     * Count errors by type for an import.
     */
    @Query("SELECT aie.errorType, COUNT(aie) " +
           "FROM AttendanceImportError aie " +
           "WHERE aie.importLog.id = :importLogId " +
           "GROUP BY aie.errorType")
    List<Object[]> countByErrorType(@Param("importLogId") Long importLogId);

    /**
     * Delete all errors for an import log.
     */
    void deleteByImportLogId(Long importLogId);
}
