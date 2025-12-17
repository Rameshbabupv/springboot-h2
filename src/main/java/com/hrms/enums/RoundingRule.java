package com.hrms.enums;

/**
 * Rounding Rule for payhead calculations
 *
 * Defines how calculated payhead values should be rounded
 *
 * @author Claude Haiku 4.5
 * @since December 17, 2025
 */
public enum RoundingRule {
    NONE,       // No rounding (keep as-is)
    ROUND,      // Standard rounding (0.5 rounds up)
    FLOOR,      // Always round down
    CEIL,       // Always round up
    ROUND_10,   // Round to nearest 10
    ROUND_100   // Round to nearest 100
}
