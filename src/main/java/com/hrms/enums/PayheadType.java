package com.hrms.enums;

/**
 * Payhead Type Enumeration
 *
 * Categorizes salary components into three types:
 * - EARNING: Components that add to employee salary (BASIC, HRA, DA, etc.)
 * - DEDUCTION: Components deducted from salary (EPF, ESIC, PT, TDS, etc.)
 * - STATUTORY: Employer contributions not paid to employee (ER_PF, ER_ESI, Gratuity)
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
public enum PayheadType {
    /**
     * Earning components - Add to gross salary
     * Examples: BASIC, HRA, DA, Conveyance, Medical Allowance
     */
    EARNING,

    /**
     * Deduction components - Subtract from gross salary
     * Examples: EPF, ESIC, Professional Tax, TDS
     */
    DEDUCTION,

    /**
     * Statutory employer contributions - Add to CTC but not paid to employee
     * Examples: Employer PF, Employer ESI, Gratuity
     */
    STATUTORY
}
