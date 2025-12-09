package com.hrms.enums;

/**
 * Status of attendance import operations.
 */
public enum ImportStatus {
    PENDING,      // Import queued but not started
    PROCESSING,   // Import in progress
    SUCCESS,      // All records imported successfully
    PARTIAL,      // Some records imported, some failed
    FAILED        // All records failed
}
