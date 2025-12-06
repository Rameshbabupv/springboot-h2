package com.hrms.enums;

/**
 * Defines when leave credits are applied to employee balances.
 */
public enum CreditTiming {
    YEAR_START,      // Credit at leave year start
    MONTH_START,     // Credit at month start
    MONTH_END,       // Credit at month end
    CONTINUOUS,      // Credit as earned
    AFTER_PROBATION, // Credit after confirmation
    QUARTERLY        // Credit every quarter
}
