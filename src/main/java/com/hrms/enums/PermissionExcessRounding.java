package com.hrms.enums;

/**
 * Defines how excess permission hours are rounded when converting to leave.
 * Used for PERMISSION category leave types with CONVERT_TO_LEAVE excess handling.
 */
public enum PermissionExcessRounding {
    ROUND_UP,            // Round up to next full unit (e.g., 3.2 hours → 4 hours)
    ROUND_DOWN,          // Round down to previous full unit (e.g., 3.8 hours → 3 hours)
    ROUND_NEAREST_HALF   // Round to nearest 0.5 (e.g., 3.2 → 3.0, 3.7 → 3.5)
}
