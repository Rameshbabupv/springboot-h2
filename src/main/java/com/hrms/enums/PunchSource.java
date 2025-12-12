package com.hrms.enums;

/**
 * Source of attendance punch entry.
 * 
 * @since 1.0 (original values)
 * @since 2.0 (ADR-002: Expanded with specific source tracking)
 */
public enum PunchSource {
    
    // ========== ADR-002: New Source Values (Primary) ==========
    
    /**
     * Biometric devices synced via Supabase cloud.
     * SHA256 hash provided by Supabase (content-addressable).
     * @since 2.0 (ADR-002)
     */
    SUPABASE,
    
    /**
     * HR/Admin manual entry via web portal.
     * SHA256 is NULL (no device_data).
     * Uses time-window deduplication as fallback.
     * @since 2.0 (ADR-002)
     */
    HR_MANUAL,
    
    /**
     * Employee self-service portal entries.
     * SHA256 calculated locally from form data.
     * @since 2.0 (ADR-002)
     */
    USER_PORTAL,
    
    /**
     * External API integrations (third-party systems).
     * SHA256 calculated from API request payload.
     * @since 2.0 (ADR-002)
     */
    REST_API,
    
    /**
     * Bulk CSV/Excel imports (admin uploads).
     * SHA256 calculated per row from imported data.
     * @since 2.0 (ADR-002)
     */
    IMPORT,
    
    /**
     * Mobile app punches (employee mobile app).
     * SHA256 calculated from mobile payload.
     * @since 2.0 (ADR-002)
     */
    MOBILE,
    
    /**
     * Pre-ADR-002 data (legacy BIOMETRIC entries).
     * Migrated from old BIOMETRIC source.
     * SHA256 may be NULL (old data without hash).
     * Mark for eventual cleanup after validation.
     * @since 2.0 (ADR-002 migration)
     */
    LEGACY,
    
    // ========== Deprecated Values (Backward Compatibility) ==========
    
    /**
     * @deprecated Use SUPABASE or LEGACY instead.
     * Kept for backward compatibility during transition.
     * Will be removed in version 3.0.
     */
    @Deprecated
    BIOMETRIC,
    
    /**
     * @deprecated Use HR_MANUAL instead.
     * Kept for backward compatibility.
     * Will be removed in version 3.0.
     */
    @Deprecated
    MANUAL,
    
    /**
     * @deprecated Use USER_PORTAL instead.
     * Kept for backward compatibility.
     * Will be removed in version 3.0.
     */
    @Deprecated
    PORTAL,
    
    /**
     * @deprecated Use IMPORT instead.
     * Kept for backward compatibility.
     * Will be removed in version 3.0.
     */
    @Deprecated
    EXCEL_IMPORT,
    
    /**
     * @deprecated Use REST_API instead.
     * Kept for backward compatibility.
     * Will be removed in version 3.0.
     */
    @Deprecated
    API
}
