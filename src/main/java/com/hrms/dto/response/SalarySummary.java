package com.hrms.dto.response;

import com.hrms.entity.EmployeeSalaryStructure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for salary summary calculations
 * Contains calculated totals and component breakdown
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySummary {

    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal totalStatutory;

    private BigDecimal grossSalary;  // Total Earnings
    private BigDecimal netSalary;    // Gross - Deductions
    private BigDecimal ctc;          // Gross + Statutory

    private List<EmployeeSalaryStructure> components;
}
