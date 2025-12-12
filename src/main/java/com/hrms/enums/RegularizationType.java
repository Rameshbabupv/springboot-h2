package com.hrms.enums;

/**
 * Type of attendance regularization request.
 */
public enum RegularizationType {
    MISSING_IN,      // Employee forgot to punch IN
    MISSING_OUT,     // Employee forgot to punch OUT
    MISSED_PUNCH,    // Both IN and OUT punches missing
    WRONG_PUNCH,     // Incorrect punch time correction
    LATE_ENTRY,      // Late arrival regularization
    EARLY_EXIT,      // Early departure regularization
    ON_DUTY,         // Working outside office (client visit, etc.)
    SYSTEM_ERROR,    // System/device malfunction
    OTHER            // Other special cases
}
