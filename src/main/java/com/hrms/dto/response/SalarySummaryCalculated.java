package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for batch salary calculation response (without persistence)
 *
 * Includes:
 * - Calculated totals (earnings, deductions, statutory, gross, net, CTC)
 * - Per-component breakdown with calculated amounts
 * - Validation warnings and errors
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySummaryCalculated {

    // ==================== Calculated Totals ====================

    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal totalStatutory;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private BigDecimal ctc;

    // ==================== Component Breakdown ====================

    @Builder.Default
    private List<CalculatedComponent> componentBreakdown = new ArrayList<>();

    // ==================== Validation Results ====================

    /**
     * Non-blocking warnings (calculation proceeds, but user should be aware)
     * Examples:
     * - "Net salary is negative (deductions exceed earnings)"
     * - "Basic salary is below minimum wage"
     * - "No statutory contributions configured"
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * Blocking errors (calculation failed or data is invalid)
     * Examples:
     * - "Duplicate payhead: Basic Salary appears twice"
     * - "Missing mandatory payhead: Basic Salary is required"
     * - "Invalid percentage: HRA requires Basic Salary but it's not present"
     * - "Payhead not found: ID 999"
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    // ==================== Validation Status ====================

    /**
     * Indicates if the calculation is valid and can be saved
     */
    private Boolean isValid;

    /**
     * Total number of components processed
     */
    private Integer totalComponents;
}
