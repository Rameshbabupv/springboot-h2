package com.hrms.graphql.input;

import com.hrms.dto.LeaveEntitlement;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Input type for creating/updating leave policy templates.
 * Used in createLeavePolicyTemplate and updateLeavePolicyTemplate mutations.
 */
@Data
public class LeavePolicyTemplateInput {
    private String code;                    // Policy code (max 50 chars, uppercase alphanumeric)
    private String name;                    // Policy name (max 200 chars)
    private String description;             // Optional description
    private Integer leaveYearStart;         // Leave year start month (1-12, default 1 for January)
    private String criteria;                // Criteria as JSON string (departments, designations, grades, etc.)
    private List<LeaveEntitlement> entitlements;  // Array of leave entitlements
    private LocalDate effectiveFrom;        // Effective from date
    private LocalDate effectiveTo;          // Effective to date (optional)
    private Boolean isActive;               // Active status
    private Boolean isDefault;              // Is default template
    private Integer priority;               // Priority for matching (higher = higher precedence)
    private String changeNotes;             // Version change documentation (max 500 chars)
}
