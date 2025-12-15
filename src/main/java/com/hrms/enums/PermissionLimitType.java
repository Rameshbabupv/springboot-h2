package com.hrms.enums;

/**
 * Defines how permission limits are enforced.
 * Used for PERMISSION category leave types.
 */
public enum PermissionLimitType {
    INSTANCES,        // Limit by number of instances per month
    HOURS,           // Limit by total hours per month
    WHICHEVER_FIRST  // Limit by whichever limit is reached first
}
