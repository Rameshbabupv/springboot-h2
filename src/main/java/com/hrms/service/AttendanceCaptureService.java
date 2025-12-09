package com.hrms.service;

import com.hrms.dto.response.ImportResult;
import com.hrms.dto.response.ValidationResult;
import com.hrms.graphql.input.ExcelRowInput;

import java.util.List;

/**
 * Service interface for Attendance Capture operations (Excel import).
 */
public interface AttendanceCaptureService {

    /**
     * Validate Excel import data before processing.
     * Checks employee IDs, dates, statuses, and time formats.
     * @param tenantId Tenant ID
     * @param companyId Company ID
     * @param rows List of Excel row inputs
     * @return Validation result with warnings and errors
     */
    ValidationResult validateAttendanceImport(String tenantId, Long companyId, List<ExcelRowInput> rows);

    /**
     * Import attendance data from Excel.
     * @param tenantId Tenant ID
     * @param companyId Company ID
     * @param rows List of Excel row inputs
     * @param overwriteExisting If true, overwrite existing attendance entries
     * @param fileName Name of the imported file (for logging)
     * @param fileSizeBytes Size of the file in bytes (for logging)
     * @param importedByUserId User ID who initiated the import (for logging)
     * @return Import result with counts and errors
     */
    ImportResult importAttendanceFromExcel(String tenantId, Long companyId, List<ExcelRowInput> rows,
                                            Boolean overwriteExisting, String fileName, Long fileSizeBytes,
                                            Long importedByUserId);
}
