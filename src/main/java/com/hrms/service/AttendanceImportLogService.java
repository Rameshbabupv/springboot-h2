package com.hrms.service;

import com.hrms.entity.AttendanceImportError;
import com.hrms.entity.AttendanceImportLog;
import com.hrms.enums.ImportStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for AttendanceImportLog operations.
 */
public interface AttendanceImportLogService {

    /**
     * Create a new import log entry.
     */
    AttendanceImportLog createImportLog(String tenantId, Long companyId, String fileName,
                                         Long fileSizeBytes, int totalRows, Long importedByUserId);

    /**
     * Update import log with results.
     */
    AttendanceImportLog updateImportResults(Long importLogId, int importedCount,
                                             int skippedCount, List<AttendanceImportError> errors);

    /**
     * Mark import as failed.
     */
    AttendanceImportLog markImportFailed(Long importLogId, String errorMessage);

    /**
     * Get import history with pagination and filters.
     */
    ImportLogPage getImportHistory(String tenantId, Long companyId, int limit, int offset,
                                    ImportStatus status, OffsetDateTime fromDate, OffsetDateTime toDate);

    /**
     * Get single import details.
     */
    Optional<AttendanceImportLog> getImportDetails(String tenantId, Long importId);

    /**
     * Get errors for a specific import.
     */
    List<AttendanceImportError> getImportErrors(String tenantId, Long importId, int limit, int offset);

    /**
     * Page result for import logs.
     */
    record ImportLogPage(
        List<AttendanceImportLog> items,
        long totalCount,
        PageInfo pageInfo
    ) {}

    /**
     * Page info for pagination.
     */
    record PageInfo(
        boolean hasNextPage,
        boolean hasPreviousPage,
        int currentPage,
        int totalPages
    ) {}
}
