package com.hrms.service;

import java.time.LocalDate;

/**
 * Service interface for syncing biometric data from Supabase to HRMS.
 */
public interface BiometricSyncService {

    /**
     * Sync all pending biometric data from Supabase.
     *
     * @param tenantId Tenant ID to sync for
     * @param companyId Company ID
     * @return BiometricSyncResult with counts of processed records
     */
    BiometricSyncResult syncFromSupabase(String tenantId, Long companyId);

    /**
     * Sync biometric data for a specific date range.
     *
     * @param tenantId Tenant ID
     * @param companyId Company ID
     * @param fromDate Start date
     * @param toDate End date
     * @return BiometricSyncResult with counts
     */
    BiometricSyncResult syncFromSupabase(String tenantId, Long companyId, LocalDate fromDate, LocalDate toDate);

    /**
     * Test connection to Supabase.
     *
     * @return true if connection successful
     */
    boolean testSupabaseConnection();

    /**
     * Get count of records in Supabase.
     *
     * @return total record count
     */
    int getSupabaseRecordCount();

    /**
     * Result of biometric sync operation.
     */
    record BiometricSyncResult(
        int totalFetched,
        int inserted,
        int skippedDuplicate,
        int skippedNoEmployee,
        int failed,
        String message
    ) {}
}
