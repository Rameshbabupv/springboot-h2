package com.hrms.enums;

/**
 * Leave type classification categories.
 * Determines how leaves are credited and managed.
 */
public enum LeaveCategory {
    EARNED,        // Accrued based on working days
    DEFINED,       // Fixed annual quota
    UNLIMITED,     // No limit (LOP, etc.)
    COMPENSATORY   // Comp-off, custom crediting
}
