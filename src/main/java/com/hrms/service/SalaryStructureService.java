package com.hrms.service;

import com.hrms.dto.response.SalarySummary;
import com.hrms.dto.response.SalarySummaryCalculated;
import com.hrms.entity.EmployeeSalaryStructure;
import com.hrms.graphql.input.SalaryComponentInput;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Employee Salary Structure operations
 *
 * Handles:
 * - Saving/updating employee salary structures
 * - Salary calculations (Gross, Net, CTC)
 * - Salary revisions with historical tracking
 * - Summary generation
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
public interface SalaryStructureService {

    /**
     * Get active salary structure for employee
     */
    List<EmployeeSalaryStructure> getEmployeeSalaryStructure(
        String tenantId,
        Long companyId,
        Long employeeId,
        LocalDate effectiveDate
    );

    /**
     * Calculate salary summary for employee
     */
    SalarySummary calculateSalarySummary(
        String tenantId,
        Long companyId,
        Long employeeId,
        LocalDate effectiveDate
    );

    /**
     * Batch calculate salary summary without persistence (for real-time validation)
     *
     * Performs:
     * - Full salary calculation (Gross, Net, CTC)
     * - Server-side validation (duplicates, negative salary, missing mandatory, etc.)
     * - Component breakdown with calculated amounts
     *
     * Returns warnings and errors without throwing exceptions, allowing frontend to display issues
     */
    SalarySummaryCalculated batchCalculateSalary(
        String tenantId,
        Long companyId,
        List<SalaryComponentInput> salaryComponents,
        LocalDate effectiveDate
    );

    /**
     * Save employee salary structure (initial or revision)
     * Automatically calculates component amounts and updates employee summary
     */
    List<EmployeeSalaryStructure> saveEmployeeSalaryStructure(
        String tenantId,
        Long companyId,
        Long employeeId,
        List<SalaryComponentInput> salaryComponents,
        LocalDate effectiveFrom
    );

    /**
     * Revise employee salary (closes old structure, creates new one)
     */
    List<EmployeeSalaryStructure> reviseEmployeeSalary(
        String tenantId,
        Long companyId,
        Long employeeId,
        List<SalaryComponentInput> salaryComponents,
        LocalDate effectiveFrom,
        String remarks
    );
}
