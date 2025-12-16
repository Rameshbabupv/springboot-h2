package com.hrms.graphql.input;

import com.hrms.enums.LeaveCategory;
import lombok.Data;

/**
 * Input type for creating/updating leave types.
 * Used in createLeaveType and updateLeaveType mutations.
 */
@Data
public class LeaveTypeInput {
    private String code;           // Leave type code (e.g., "CL", "SL") - max 10 chars, auto-uppercase
    private String name;           // Display name (e.g., "Casual Leave") - required, max 100 chars
    private LeaveCategory category;  // Category: DEFINED, EARNED, UNLIMITED, COMPENSATORY, PERMISSION
    private String description;    // Optional description
    private String icon;           // Emoji icon (e.g., "🌴")
    private String colorCode;      // Hex color code (e.g., "#3B82F6") - must match pattern ^#[0-9A-Fa-f]{6}$
    private Integer displayOrder;  // Sorting order - positive integer
    private Boolean isActive;      // Active status - defaults to true
}
