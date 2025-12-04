package com.hrms.enums;

/**
 * Defines what happens to unused leave balance at year end.
 */
public enum YearEndAction {
    LAPSE,            // Unused leaves expire
    CARRY_FORWARD,    // Carry to next year
    ENCASH,           // Convert to cash
    CARRY_AND_ENCASH, // Partial carry + encash
    NONE              // Not applicable (for unlimited types)
}
