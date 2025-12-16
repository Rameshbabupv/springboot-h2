package com.hrms.enums;

/**
 * Defines how leave credits are calculated and applied (frequency of crediting).
 * Used in leave policy templates to specify credit methodology.
 */
public enum CreditMethod {
    ANNUAL,        // Fixed days per year (credited at year start)
    MONTHLY,       // Fixed days per month (credited monthly)
    QUARTERLY,     // Fixed days per quarter (credited quarterly)
    WORKING_DAYS,  // Earn based on attendance (continuous accrual)
    UNLIMITED,     // No quota (LOP, no crediting)
    PERMISSION,    // Permission type (hour/instance based limits)
    COMPENSATORY,  // Comp-off (credited on approval)
    CUSTOM         // Manual management
}
