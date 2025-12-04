package com.hrms.enums;

/**
 * Defines when carried forward leave balance expires.
 */
public enum CarryForwardExpiry {
    NEVER,     // Never expires
    Q1,        // Expires end of Q1
    Q2,        // Expires end of Q2
    H1,        // Expires end of H1 (6 months)
    YEAR_END   // Expires at year end
}
