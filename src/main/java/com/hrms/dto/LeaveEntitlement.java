package com.hrms.dto;

import com.hrms.enums.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Leave entitlement configuration for a specific leave type within a policy template.
 * Stored as JSONB in leave_policy_templates.entitlements array.
 *
 * Different fields are relevant based on leave category (DEFINED, EARNED, UNLIMITED, COMPENSATORY, PERMISSION).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveEntitlement {

    // Core fields (all categories)
    private Long leaveTypeId;          // Optional: Numeric ID (backward compatibility)
    private String leaveTypeCode;       // Optional: Code (preferred) - at least one must be provided
    private Boolean isEnabled;
    private Boolean isPaid;

    // DEFINED category fields
    private CreditMethod creditFrequency;      // ANNUAL, MONTHLY, QUARTERLY
    private Double annualQuota;                 // For ANNUAL frequency
    private Double monthlyCredit;               // For MONTHLY frequency
    private Double quarterlyCredit;             // For QUARTERLY frequency
    private CreditTiming creditTiming;          // When credits are applied

    // EARNED category fields
    private Integer workingDaysPerCredit;       // Days worked to earn 1 leave
    private Double creditPerWorkingDays;        // Leaves earned per working days

    // Year-end handling (DEFINED, EARNED categories)
    private YearEndAction yearEndAction;        // LAPSE, CARRY_FORWARD, ENCASH, etc.
    private Double maxCarryForward;             // Max days to carry forward (0 = unlimited)
    private CarryForwardExpiry carryForwardExpiry;  // When carried leaves expire
    private Boolean encashmentAllowed;          // Can encash unused leaves
    private Double maxEncashment;               // Max days to encash (0 = unlimited)

    // Pro-rata and restrictions
    private Boolean proRataOnJoining;           // Pro-rate for mid-year joiners
    private Double minDaysPerApplication;       // Minimum leave days per application
    private Double maxDaysPerApplication;       // Maximum leave days per application
    private Integer advanceNoticeDays;          // Days of advance notice required
    private Integer maxConsecutiveDays;         // Max consecutive days allowed

    // Application settings
    private Boolean allowHalfDay;               // Allow half-day leave
    private Boolean allowHourly;                // Allow hourly leave
    private Boolean requiresAttachment;         // Attachment mandatory
    private Integer attachmentRequiredAfterDays; // Attachment required if > X days

    // Payhead links (for payroll integration)
    private String lopPayheadCode;              // LOP payhead code
    private String encashPayheadCode;           // Encashment payhead code

    // Comp-Off specific (COMPENSATORY category)
    private Integer compOffExpiryDays;          // Days after which comp-off expires
    private Double compOffMinHoursFullDay;      // Min hours worked for full day comp-off
    private Double compOffMinHoursHalfDay;      // Min hours worked for half day comp-off

    // Permission specific (PERMISSION category)
    private Integer permissionMaxInstancesPerMonth;  // Max permission instances per month
    private Double permissionMaxHoursPerMonth;       // Max permission hours per month
    private Double permissionMaxHoursPerInstance;    // Max hours per permission instance
    private PermissionLimitType permissionLimitType; // INSTANCES, HOURS, WHICHEVER_FIRST
    private PermissionExcessHandling permissionExcessHandling; // BLOCK, LAPSE, CONVERT_TO_LEAVE
    private PermissionExcessRounding permissionExcessRounding; // Rounding logic for conversion
    private Double permissionConversionRate;         // Hours to convert to 1 day leave
    private String permissionDeductFromLeaveType;    // Leave type to deduct when excess
    private String permissionFallbackLeaveType;      // Fallback if deduct leave exhausted
}
