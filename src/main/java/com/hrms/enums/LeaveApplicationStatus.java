package com.hrms.enums;

/**
 * Leave application workflow status.
 */
public enum LeaveApplicationStatus {
    DRAFT,      // Saved but not submitted
    PENDING,    // Submitted, awaiting approval
    APPROVED,   // Approved by manager
    REJECTED,   // Rejected by manager
    CANCELLED,  // Cancelled by approver
    WITHDRAWN   // Withdrawn by employee
}
