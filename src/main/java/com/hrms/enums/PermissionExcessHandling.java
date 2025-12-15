package com.hrms.enums;

/**
 * Defines how excess permission hours/instances are handled.
 * Used for PERMISSION category leave types.
 */
public enum PermissionExcessHandling {
    BLOCK,             // Block application if limit exceeded
    LAPSE,             // Allow but excess hours lapse (no deduction)
    CONVERT_TO_LEAVE   // Convert excess to leave deduction
}
