package com.hrms.enums;

/**
 * Calculation Type Enumeration
 *
 * Defines how a salary component value is calculated:
 * - FIXED: Absolute fixed amount (e.g., ₹1600 for Conveyance)
 * - PERCENTAGE: Percentage of a base amount (e.g., 40% of BASIC for HRA)
 * - FORMULA: Complex calculation using formula (future enhancement)
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
public enum CalculationType {
    /**
     * Fixed absolute amount
     * Example: Conveyance Allowance = ₹1600
     */
    FIXED,

    /**
     * Percentage of base amount (BASIC, GROSS, or CTC)
     * Example: HRA = 40% of BASIC
     */
    PERCENTAGE,

    /**
     * Formula-based calculation (for future complex calculations)
     * Example: (BASIC * 0.4) + (DA * 0.2) + 500
     */
    FORMULA
}
