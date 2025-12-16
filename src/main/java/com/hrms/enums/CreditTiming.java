package com.hrms.enums;

/**
 * Defines when leave credits are applied to employee balances.
 * Used in leave policy templates to specify credit timing.
 */
public enum CreditTiming {
    YEAR_START,      // Credit at leave year start
    MONTH_START,     // Credit at month start
    MONTH_END,       // Credit at month end
    QUARTER_START,   // Credit at quarter start
    QUARTER_END,     // Credit at quarter end
    CONTINUOUS,      // Credit as earned (for working days based)
    AFTER_PROBATION  // Credit after probation/confirmation
}
