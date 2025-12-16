package com.hrms.service.impl;

import com.hrms.dto.response.CalculatedComponent;
import com.hrms.dto.response.SalarySummary;
import com.hrms.dto.response.SalarySummaryCalculated;
import com.hrms.entity.Company;
import com.hrms.entity.Employee;
import com.hrms.entity.EmployeeSalaryStructure;
import com.hrms.entity.PayheadMaster;
import com.hrms.enums.PayheadType;
import com.hrms.exception.BadRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.graphql.input.SalaryComponentInput;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.EmployeeSalaryStructureRepository;
import com.hrms.repository.PayheadMasterRepository;
import com.hrms.service.SalaryStructureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;


/**
 * Service implementation for Employee Salary Structure with calculation logic
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final EmployeeSalaryStructureRepository salaryStructureRepository;
    private final PayheadMasterRepository payheadRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSalaryStructure> getEmployeeSalaryStructure(
            String tenantId, Long companyId, Long employeeId, LocalDate effectiveDate) {

        log.debug("Fetching salary structure - employee: {}, date: {}", employeeId, effectiveDate);

        // Validate employee exists and belongs to tenant/company
        validateEmployeeAccess(tenantId, companyId, employeeId);

        if (effectiveDate != null) {
            return salaryStructureRepository.findByEmployeeIdAndEffectiveDate(employeeId, effectiveDate);
        } else {
            return salaryStructureRepository.findActiveByEmployeeId(employeeId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SalarySummary calculateSalarySummary(
            String tenantId, Long companyId, Long employeeId, LocalDate effectiveDate) {

        log.debug("Calculating salary summary - employee: {}, date: {}", employeeId, effectiveDate);

        List<EmployeeSalaryStructure> components = getEmployeeSalaryStructure(
            tenantId, companyId, employeeId, effectiveDate);

        return buildSalarySummary(components);
    }

    @Override
    @Transactional(readOnly = true)
    public SalarySummaryCalculated batchCalculateSalary(
            String tenantId,
            Long companyId,
            List<SalaryComponentInput> salaryComponents,
            LocalDate effectiveDate) {

        log.debug("Batch calculating salary - tenant: {}, company: {}, components: {}",
                  tenantId, companyId, salaryComponents.size());

        SalarySummaryCalculated.SalarySummaryCalculatedBuilder builder = SalarySummaryCalculated.builder();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<CalculatedComponent> componentBreakdown = new ArrayList<>();

        // Validation 1: Check for empty components
        if (salaryComponents == null || salaryComponents.isEmpty()) {
            errors.add("At least one salary component is required");
            return builder
                .errors(errors)
                .warnings(warnings)
                .componentBreakdown(componentBreakdown)
                .isValid(false)
                .totalComponents(0)
                .totalEarnings(BigDecimal.ZERO)
                .totalDeductions(BigDecimal.ZERO)
                .totalStatutory(BigDecimal.ZERO)
                .grossSalary(BigDecimal.ZERO)
                .netSalary(BigDecimal.ZERO)
                .ctc(BigDecimal.ZERO)
                .build();
        }

        // Validation 2: Check for duplicate payheads
        Set<Long> payheadIds = new HashSet<>();
        for (SalaryComponentInput input : salaryComponents) {
            if (!payheadIds.add(input.getPayheadId())) {
                // Find payhead name for better error message
                try {
                    PayheadMaster payhead = payheadRepository.findById(input.getPayheadId()).orElse(null);
                    String payheadName = payhead != null ? payhead.getPayheadName() : "ID: " + input.getPayheadId();
                    errors.add("Duplicate payhead: " + payheadName + " appears multiple times");
                } catch (Exception e) {
                    errors.add("Duplicate payhead with ID: " + input.getPayheadId());
                }
            }
        }

        // Validation 3: Verify all payheads exist and are valid
        Map<Long, PayheadMaster> payheadMap = new HashMap<>();
        for (SalaryComponentInput input : salaryComponents) {
            try {
                PayheadMaster payhead = payheadRepository.findById(input.getPayheadId()).orElse(null);
                if (payhead == null) {
                    errors.add("Payhead not found with ID: " + input.getPayheadId());
                } else if (!payhead.getIsActive()) {
                    warnings.add("Payhead '" + payhead.getPayheadName() + "' is inactive");
                    payheadMap.put(input.getPayheadId(), payhead);
                } else {
                    payheadMap.put(input.getPayheadId(), payhead);
                }
            } catch (Exception e) {
                errors.add("Error fetching payhead with ID: " + input.getPayheadId());
            }
        }

        // Validation 4: Check for mandatory payheads
        try {
            List<PayheadMaster> mandatoryPayheads = payheadRepository
                .findMandatoryPayheads(tenantId, companyId);

            for (PayheadMaster mandatory : mandatoryPayheads) {
                boolean found = salaryComponents.stream()
                    .anyMatch(input -> input.getPayheadId().equals(mandatory.getId()));
                if (!found) {
                    errors.add("Missing mandatory payhead: " + mandatory.getPayheadName() + " is required");
                }
            }
        } catch (Exception e) {
            log.warn("Could not validate mandatory payheads: {}", e.getMessage());
        }

        // If critical errors exist, return early
        if (!errors.isEmpty()) {
            return builder
                .errors(errors)
                .warnings(warnings)
                .componentBreakdown(componentBreakdown)
                .isValid(false)
                .totalComponents(salaryComponents.size())
                .totalEarnings(BigDecimal.ZERO)
                .totalDeductions(BigDecimal.ZERO)
                .totalStatutory(BigDecimal.ZERO)
                .grossSalary(BigDecimal.ZERO)
                .netSalary(BigDecimal.ZERO)
                .ctc(BigDecimal.ZERO)
                .build();
        }

        // ==================== CALCULATION PHASE ====================

        Map<String, BigDecimal> calculationCache = new HashMap<>();
        BigDecimal totalEarnings = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalStatutory = BigDecimal.ZERO;
        boolean hasBasicSalary = false;

        // Step 1: Calculate FIXED earnings first (especially BASIC)
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadMap.get(input.getPayheadId());
            if (payhead == null) continue;

            if (payhead.getPayheadType() == PayheadType.EARNING &&
                "FIXED".equals(input.getValueType())) {

                BigDecimal amount = input.getFixedAmount() != null ? input.getFixedAmount() : BigDecimal.ZERO;
                totalEarnings = totalEarnings.add(amount);

                // Track BASIC for percentage calculations
                if ("BASIC".equals(payhead.getPayheadCode())) {
                    hasBasicSalary = true;
                    calculationCache.put("BASIC", amount);
                }

                componentBreakdown.add(CalculatedComponent.builder()
                    .payheadId(payhead.getId())
                    .payheadName(payhead.getPayheadName())
                    .payheadCode(payhead.getPayheadCode())
                    .payheadType(payhead.getPayheadType())
                    .valueType("FIXED")
                    .inputValue(amount)
                    .calculatedAmount(amount)
                    .calculationBase(null)
                    .build());
            }
        }

        calculationCache.put("GROSS", totalEarnings);

        // Step 2: Calculate PERCENTAGE earnings
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadMap.get(input.getPayheadId());
            if (payhead == null) continue;

            if (payhead.getPayheadType() == PayheadType.EARNING &&
                "PERCENTAGE".equals(input.getValueType())) {

                // Validation 5: Check if calculation base exists
                String calcBase = payhead.getCalculationBase() != null ? payhead.getCalculationBase() : "BASIC";
                if (!calculationCache.containsKey(calcBase)) {
                    errors.add("Invalid percentage calculation: " + payhead.getPayheadName() +
                              " requires " + calcBase + " but it's not present");
                    continue;
                }

                BigDecimal base = calculationCache.get(calcBase);
                BigDecimal percentage = input.getPercentage() != null ? input.getPercentage() : BigDecimal.ZERO;
                BigDecimal amount = base.multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                totalEarnings = totalEarnings.add(amount);
                calculationCache.put("GROSS", totalEarnings);

                componentBreakdown.add(CalculatedComponent.builder()
                    .payheadId(payhead.getId())
                    .payheadName(payhead.getPayheadName())
                    .payheadCode(payhead.getPayheadCode())
                    .payheadType(payhead.getPayheadType())
                    .valueType("PERCENTAGE")
                    .inputValue(percentage)
                    .calculatedAmount(amount)
                    .calculationBase(calcBase)
                    .build());
            }
        }

        // Step 3: Calculate deductions (both FIXED and PERCENTAGE)
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadMap.get(input.getPayheadId());
            if (payhead == null) continue;

            if (payhead.getPayheadType() == PayheadType.DEDUCTION) {
                BigDecimal amount;
                String calcBase = null;

                if ("FIXED".equals(input.getValueType())) {
                    amount = input.getFixedAmount() != null ? input.getFixedAmount() : BigDecimal.ZERO;
                } else {
                    // Validation 6: Check if calculation base exists for percentage deductions
                    calcBase = payhead.getCalculationBase() != null ? payhead.getCalculationBase() : "BASIC";
                    if (!calculationCache.containsKey(calcBase)) {
                        errors.add("Invalid percentage calculation: " + payhead.getPayheadName() +
                                  " requires " + calcBase + " but it's not present");
                        continue;
                    }

                    BigDecimal base = calculationCache.get(calcBase);
                    BigDecimal percentage = input.getPercentage() != null ? input.getPercentage() : BigDecimal.ZERO;
                    amount = base.multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                totalDeductions = totalDeductions.add(amount);

                componentBreakdown.add(CalculatedComponent.builder()
                    .payheadId(payhead.getId())
                    .payheadName(payhead.getPayheadName())
                    .payheadCode(payhead.getPayheadCode())
                    .payheadType(payhead.getPayheadType())
                    .valueType(input.getValueType())
                    .inputValue("FIXED".equals(input.getValueType()) ? input.getFixedAmount() : input.getPercentage())
                    .calculatedAmount(amount)
                    .calculationBase(calcBase)
                    .build());
            }
        }

        // Step 4: Calculate statutory contributions (employer)
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadMap.get(input.getPayheadId());
            if (payhead == null) continue;

            if (payhead.getPayheadType() == PayheadType.STATUTORY) {
                BigDecimal amount;
                String calcBase = null;

                if ("FIXED".equals(input.getValueType())) {
                    amount = input.getFixedAmount() != null ? input.getFixedAmount() : BigDecimal.ZERO;
                } else {
                    calcBase = payhead.getCalculationBase() != null ? payhead.getCalculationBase() : "BASIC";
                    if (!calculationCache.containsKey(calcBase)) {
                        errors.add("Invalid percentage calculation: " + payhead.getPayheadName() +
                                  " requires " + calcBase + " but it's not present");
                        continue;
                    }

                    BigDecimal base = calculationCache.get(calcBase);
                    BigDecimal percentage = input.getPercentage() != null ? input.getPercentage() : BigDecimal.ZERO;
                    amount = base.multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                totalStatutory = totalStatutory.add(amount);

                componentBreakdown.add(CalculatedComponent.builder()
                    .payheadId(payhead.getId())
                    .payheadName(payhead.getPayheadName())
                    .payheadCode(payhead.getPayheadCode())
                    .payheadType(payhead.getPayheadType())
                    .valueType(input.getValueType())
                    .inputValue("FIXED".equals(input.getValueType()) ? input.getFixedAmount() : input.getPercentage())
                    .calculatedAmount(amount)
                    .calculationBase(calcBase)
                    .build());
            }
        }

        // ==================== FINAL CALCULATIONS & VALIDATIONS ====================

        BigDecimal grossSalary = totalEarnings;
        BigDecimal netSalary = grossSalary.subtract(totalDeductions);
        BigDecimal ctc = grossSalary.add(totalStatutory);

        // Validation 7: Check for negative net salary
        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            warnings.add("Net salary is negative (₹" + netSalary + "). Deductions exceed earnings.");
        }

        // Validation 8: Warning if no earnings
        if (totalEarnings.compareTo(BigDecimal.ZERO) == 0) {
            warnings.add("No earning components defined. Total salary is zero.");
        }

        // Validation 9: Warning if BASIC salary not present for percentage calculations
        if (!hasBasicSalary) {
            boolean hasPercentageComponents = salaryComponents.stream()
                .anyMatch(input -> "PERCENTAGE".equals(input.getValueType()));
            if (hasPercentageComponents) {
                warnings.add("Basic Salary not defined. Percentage calculations may be inaccurate.");
            }
        }

        // Validation 10: Warning if no statutory contributions
        if (totalStatutory.compareTo(BigDecimal.ZERO) == 0) {
            warnings.add("No statutory contributions (employer) configured. CTC = Gross Salary.");
        }

        boolean isValid = errors.isEmpty();

        return builder
            .totalEarnings(totalEarnings)
            .totalDeductions(totalDeductions)
            .totalStatutory(totalStatutory)
            .grossSalary(grossSalary)
            .netSalary(netSalary)
            .ctc(ctc)
            .componentBreakdown(componentBreakdown)
            .errors(errors)
            .warnings(warnings)
            .isValid(isValid)
            .totalComponents(salaryComponents.size())
            .build();
    }

    @Override
    public List<EmployeeSalaryStructure> saveEmployeeSalaryStructure(
            String tenantId,
            Long companyId,
            Long employeeId,
            List<SalaryComponentInput> salaryComponents,
            LocalDate effectiveFrom) {

        log.info("Saving salary structure - employee: {}, components: {}, effective: {}",
                 employeeId, salaryComponents.size(), effectiveFrom);

        // Step 1: Validate inputs
        Employee employee = validateEmployeeAccess(tenantId, companyId, employeeId);
        Company company = employee.getCompany();
        validateSalaryComponents(tenantId, companyId, salaryComponents);

        // Step 2: Deactivate existing active salary structure
        deactivateExistingStructure(employeeId, effectiveFrom);

        // Step 3: Create new salary structure with calculations
        List<EmployeeSalaryStructure> structures = calculateAndCreateStructures(
            tenantId, company, employee, salaryComponents, effectiveFrom);

        // Step 4: Save all structures
        structures = salaryStructureRepository.saveAll(structures);

        // Step 5: Update employee summary fields
        updateEmployeeSummary(employee, structures, effectiveFrom);

        log.info("Salary structure saved successfully - {} components created", structures.size());
        return structures;
    }

    @Override
    public List<EmployeeSalaryStructure> reviseEmployeeSalary(
            String tenantId,
            Long companyId,
            Long employeeId,
            List<SalaryComponentInput> salaryComponents,
            LocalDate effectiveFrom,
            String remarks) {

        log.info("Revising salary - employee: {}, effective: {}", employeeId, effectiveFrom);

        // Revision is same as save, but explicitly closes old records
        return saveEmployeeSalaryStructure(tenantId, companyId, employeeId, salaryComponents, effectiveFrom);
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validate employee exists and user has access
     */
    private Employee validateEmployeeAccess(String tenantId, Long companyId, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (!employee.getTenantId().equals(tenantId)) {
            throw new BadRequestException("Employee does not belong to tenant: " + tenantId);
        }

        if (companyId != null && !employee.getCompany().getId().equals(companyId)) {
            throw new BadRequestException("Employee does not belong to company: " + companyId);
        }

        return employee;
    }

    /**
     * Validate all salary components exist and are active
     */
    private void validateSalaryComponents(String tenantId, Long companyId, List<SalaryComponentInput> components) {
        if (components == null || components.isEmpty()) {
            throw new BadRequestException("At least one salary component is required");
        }

        // Check for at least one earning component
        boolean hasEarning = components.stream()
            .anyMatch(c -> {
                PayheadMaster payhead = payheadRepository.findById(c.getPayheadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payhead not found: " + c.getPayheadId()));
                return payhead.getPayheadType() == PayheadType.EARNING;
            });

        if (!hasEarning) {
            throw new BadRequestException("At least one EARNING component is required");
        }

        // Validate value types
        for (SalaryComponentInput component : components) {
            if ("FIXED".equals(component.getValueType()) && component.getFixedAmount() == null) {
                throw new BadRequestException("Fixed amount is required for FIXED value type");
            }
            if ("PERCENTAGE".equals(component.getValueType()) && component.getPercentage() == null) {
                throw new BadRequestException("Percentage is required for PERCENTAGE value type");
            }
        }
    }

    /**
     * Deactivate existing active salary structure
     */
    private void deactivateExistingStructure(Long employeeId, LocalDate effectiveFrom) {
        List<EmployeeSalaryStructure> existing = salaryStructureRepository.findActiveByEmployeeId(employeeId);

        if (!existing.isEmpty()) {
            log.debug("Deactivating {} existing salary components", existing.size());
            LocalDate effectiveTo = effectiveFrom.minusDays(1);

            for (EmployeeSalaryStructure structure : existing) {
                structure.setEffectiveTo(effectiveTo);
                structure.setIsActive(false);
            }

            salaryStructureRepository.saveAll(existing);
        }
    }

    /**
     * Calculate and create salary structures with proper calculation logic
     */
    private List<EmployeeSalaryStructure> calculateAndCreateStructures(
            String tenantId,
            Company company,
            Employee employee,
            List<SalaryComponentInput> salaryComponents,
            LocalDate effectiveFrom) {

        List<EmployeeSalaryStructure> structures = new ArrayList<>();
        Map<String, BigDecimal> calculationCache = new HashMap<>();

        // Step 1: Calculate FIXED earnings first (especially BASIC)
        BigDecimal basicSalary = BigDecimal.ZERO;
        BigDecimal totalEarnings = BigDecimal.ZERO;

        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadRepository.findById(input.getPayheadId())
                .orElseThrow(() -> new ResourceNotFoundException("Payhead not found: " + input.getPayheadId()));

            if (payhead.getPayheadType() == PayheadType.EARNING &&
                "FIXED".equals(input.getValueType())) {

                BigDecimal amount = input.getFixedAmount();
                totalEarnings = totalEarnings.add(amount);

                // Track BASIC for percentage calculations
                if ("BASIC".equals(payhead.getPayheadCode())) {
                    basicSalary = amount;
                    calculationCache.put("BASIC", basicSalary);
                }

                structures.add(createStructure(tenantId, company, employee, payhead, input, amount, effectiveFrom));
            }
        }

        calculationCache.put("GROSS", totalEarnings);
        log.debug("Initial calculations - BASIC: {}, GROSS: {}", basicSalary, totalEarnings);

        // Step 2: Calculate PERCENTAGE earnings
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadRepository.findById(input.getPayheadId())
                .orElseThrow(() -> new ResourceNotFoundException("Payhead not found: " + input.getPayheadId()));

            if (payhead.getPayheadType() == PayheadType.EARNING &&
                "PERCENTAGE".equals(input.getValueType())) {

                BigDecimal base = getCalculationBase(payhead.getCalculationBase(), calculationCache);
                BigDecimal amount = base.multiply(input.getPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                totalEarnings = totalEarnings.add(amount);
                calculationCache.put("GROSS", totalEarnings);

                structures.add(createStructure(tenantId, company, employee, payhead, input, amount, effectiveFrom));
            }
        }

        // Step 3: Calculate deductions (both FIXED and PERCENTAGE)
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadRepository.findById(input.getPayheadId())
                .orElseThrow(() -> new ResourceNotFoundException("Payhead not found: " + input.getPayheadId()));

            if (payhead.getPayheadType() == PayheadType.DEDUCTION) {
                BigDecimal amount;

                if ("FIXED".equals(input.getValueType())) {
                    amount = input.getFixedAmount();
                } else {
                    BigDecimal base = getCalculationBase(payhead.getCalculationBase(), calculationCache);
                    amount = base.multiply(input.getPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                structures.add(createStructure(tenantId, company, employee, payhead, input, amount, effectiveFrom));
            }
        }

        // Step 4: Calculate statutory contributions (employer)
        for (SalaryComponentInput input : salaryComponents) {
            PayheadMaster payhead = payheadRepository.findById(input.getPayheadId())
                .orElseThrow(() -> new ResourceNotFoundException("Payhead not found: " + input.getPayheadId()));

            if (payhead.getPayheadType() == PayheadType.STATUTORY) {
                BigDecimal amount;

                if ("FIXED".equals(input.getValueType())) {
                    amount = input.getFixedAmount();
                } else {
                    BigDecimal base = getCalculationBase(payhead.getCalculationBase(), calculationCache);
                    amount = base.multiply(input.getPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                structures.add(createStructure(tenantId, company, employee, payhead, input, amount, effectiveFrom));
            }
        }

        return structures;
    }

    /**
     * Get calculation base for percentage components
     */
    private BigDecimal getCalculationBase(String calculationBase, Map<String, BigDecimal> cache) {
        if (calculationBase == null || calculationBase.isEmpty()) {
            return cache.getOrDefault("BASIC", BigDecimal.ZERO);
        }

        return cache.getOrDefault(calculationBase, BigDecimal.ZERO);
    }

    /**
     * Create salary structure record
     */
    private EmployeeSalaryStructure createStructure(
            String tenantId,
            Company company,
            Employee employee,
            PayheadMaster payhead,
            SalaryComponentInput input,
            BigDecimal calculatedAmount,
            LocalDate effectiveFrom) {

        return EmployeeSalaryStructure.builder()
            .tenantId(tenantId)
            .company(company)
            .employee(employee)
            .payhead(payhead)
            .valueType(input.getValueType())
            .fixedAmount(input.getFixedAmount())
            .percentage(input.getPercentage())
            .calculatedAmount(calculatedAmount)
            .effectiveFrom(effectiveFrom)
            .effectiveTo(null)
            .isActive(true)
            .build();
    }

    /**
     * Build salary summary from components
     */
    private SalarySummary buildSalarySummary(List<EmployeeSalaryStructure> components) {
        BigDecimal totalEarnings = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalStatutory = BigDecimal.ZERO;

        for (EmployeeSalaryStructure component : components) {
            PayheadType type = component.getPayhead().getPayheadType();
            BigDecimal amount = component.getCalculatedAmount();

            switch (type) {
                case EARNING:
                    totalEarnings = totalEarnings.add(amount);
                    break;
                case DEDUCTION:
                    totalDeductions = totalDeductions.add(amount);
                    break;
                case STATUTORY:
                    totalStatutory = totalStatutory.add(amount);
                    break;
            }
        }

        BigDecimal grossSalary = totalEarnings;
        BigDecimal netSalary = grossSalary.subtract(totalDeductions);
        BigDecimal ctc = grossSalary.add(totalStatutory);

        return SalarySummary.builder()
            .totalEarnings(totalEarnings)
            .totalDeductions(totalDeductions)
            .totalStatutory(totalStatutory)
            .grossSalary(grossSalary)
            .netSalary(netSalary)
            .ctc(ctc)
            .components(components)
            .build();
    }

    /**
     * Update employee summary fields
     */
    private void updateEmployeeSummary(Employee employee, List<EmployeeSalaryStructure> structures, LocalDate effectiveFrom) {
        SalarySummary summary = buildSalarySummary(structures);

        employee.setGrossAmount(summary.getGrossSalary());
        employee.setTakeHome(summary.getNetSalary());
        employee.setCtc(summary.getCtc());
        employee.setEffectFromSalary(effectiveFrom);

        employeeRepository.save(employee);
        log.debug("Employee summary updated - Gross: {}, Net: {}, CTC: {}",
                  summary.getGrossSalary(), summary.getNetSalary(), summary.getCtc());
    }
}
