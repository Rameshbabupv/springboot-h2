package com.hrms.enums;

/**
 * Status of punch log entry.
 * MATCHED - Employee identified from punch
 * UNMATCHED - Biometric ID not linked to employee
 */
public enum PunchStatus {
    MATCHED,
    UNMATCHED
}
