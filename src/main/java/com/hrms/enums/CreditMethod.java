package com.hrms.enums;

/**
 * Defines how leave credits are calculated and applied.
 */
public enum CreditMethod {
    ANNUAL,        // Fixed days per year
    MONTHLY,       // Fixed days per month
    WORKING_DAYS,  // Earn based on attendance
    UNLIMITED,     // No quota (LOP)
    CUSTOM         // Manual management
}
