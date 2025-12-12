package com.hrms.service.impl;

import com.hrms.dto.response.EmployeeHolidaySelectionsResponse;
import com.hrms.dto.response.SubmitEmployeeHolidaySelectionsResponse;
import com.hrms.entity.Employee;
import com.hrms.entity.EmployeeHolidaySelection;
import com.hrms.entity.Holiday;
import com.hrms.enums.HolidayCategory;
import com.hrms.repository.EmployeeHolidaySelectionRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.HolidayRepository;
import com.hrms.service.EmployeeHolidaySelectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeeHolidaySelectionService.
 */
@Service
@Transactional
public class EmployeeHolidaySelectionServiceImpl implements EmployeeHolidaySelectionService {

    private final EmployeeHolidaySelectionRepository selectionRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;

    // TODO: Move to configuration
    private static final int DEFAULT_MAX_SELECTIONS = 3;

    public EmployeeHolidaySelectionServiceImpl(
            EmployeeHolidaySelectionRepository selectionRepository,
            EmployeeRepository employeeRepository,
            HolidayRepository holidayRepository) {
        this.selectionRepository = selectionRepository;
        this.employeeRepository = employeeRepository;
        this.holidayRepository = holidayRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Holiday> getAvailableOptionalHolidays(String tenantId, String financialYear, Long employeeId) {
        // Get employee to access company and location
        final Long companyId;
        final Long locationId;

        if (employeeId != null) {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee != null) {
                companyId = employee.getCompany() != null ? employee.getCompany().getId() : null;
                locationId = employee.getLocation() != null ? employee.getLocation().getId() : null;
            } else {
                companyId = null;
                locationId = null;
            }
        } else {
            companyId = null;
            locationId = null;
        }

        // Parse year from financial year (e.g., "2025-26" -> 2025)
        Integer year = parseFinancialYear(financialYear);

        // Get all holidays for the year with OPTIONAL category
        List<Holiday> allHolidays = holidayRepository.findByTenantIdAndCategoryOrderByDateAsc(
                tenantId, HolidayCategory.OPTIONAL);

        // Filter by year and company/location restrictions
        return allHolidays.stream()
                .filter(holiday -> holiday.getDate().getYear() == year)
                .filter(holiday -> isHolidayApplicableToEmployee(holiday, companyId, locationId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeHolidaySelectionsResponse getEmployeeHolidaySelections(
            String tenantId, Long employeeId, String financialYear) {

        List<EmployeeHolidaySelection> selections = selectionRepository
                .findByTenantAndEmployeeAndFinancialYear(tenantId, employeeId, financialYear);

        boolean isSubmitted = selectionRepository
                .isSubmittedByTenantAndEmployeeAndFinancialYear(tenantId, employeeId, financialYear);

        LocalDateTime submittedAt = selectionRepository
                .findSubmittedAtByTenantAndEmployeeAndFinancialYear(tenantId, employeeId, financialYear)
                .orElse(null);

        int selectedCount = selections.size();
        int allowedCount = DEFAULT_MAX_SELECTIONS; // TODO: Get from configuration

        return new EmployeeHolidaySelectionsResponse(
                selections, isSubmitted, submittedAt, allowedCount, selectedCount);
    }

    @Override
    @Transactional
    public SubmitEmployeeHolidaySelectionsResponse submitEmployeeHolidaySelections(
            String tenantId, Long employeeId, String financialYear, List<Long> holidayIds) {

        // Validation 1: Employee exists and is active
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found or inactive"));

        if (!employee.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        // Validation 2: Check if already submitted
        boolean alreadySubmitted = selectionRepository
                .isSubmittedByTenantAndEmployeeAndFinancialYear(tenantId, employeeId, financialYear);

        if (alreadySubmitted) {
            throw new IllegalArgumentException("Selections already submitted. Contact HR to modify.");
        }

        // Validation 3: Check count limit
        if (holidayIds.size() > DEFAULT_MAX_SELECTIONS) {
            throw new IllegalArgumentException(
                    String.format("Maximum %d selections allowed, %d provided",
                            DEFAULT_MAX_SELECTIONS, holidayIds.size()));
        }

        // Validation 4: Check for duplicates
        if (holidayIds.size() != holidayIds.stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate holiday selections not allowed");
        }

        // Validation 5: Validate all holidays exist and are OPTIONAL category
        List<Holiday> holidays = new ArrayList<>();
        Long companyId = employee.getCompany() != null ? employee.getCompany().getId() : null;
        Long locationId = employee.getLocation() != null ? employee.getLocation().getId() : null;

        for (Long holidayId : holidayIds) {
            Holiday holiday = holidayRepository.findById(holidayId)
                    .orElseThrow(() -> new IllegalArgumentException("Holiday ID " + holidayId + " not found"));

            // Check if holiday is OPTIONAL category
            if (holiday.getCategory() != HolidayCategory.OPTIONAL) {
                throw new IllegalArgumentException(
                        "Holiday ID " + holidayId + " is not an optional holiday");
            }

            // Check company/location restrictions
            if (!isHolidayApplicableToEmployee(holiday, companyId, locationId)) {
                throw new IllegalArgumentException(
                        "Holiday ID " + holidayId + " not available for your company/location");
            }

            holidays.add(holiday);
        }

        // Transaction: Delete existing selections and create new ones
        selectionRepository.deleteByTenantIdAndEmployeeIdAndFinancialYear(
                tenantId, employeeId, financialYear);

        LocalDateTime now = LocalDateTime.now();
        List<EmployeeHolidaySelection> createdSelections = new ArrayList<>();

        for (Holiday holiday : holidays) {
            EmployeeHolidaySelection selection = new EmployeeHolidaySelection(
                    tenantId, employee, holiday, financialYear, true, now);
            selection.setCreatedBy(employee.getEmpId());
            selection.setUpdatedBy(employee.getEmpId());
            createdSelections.add(selectionRepository.save(selection));
        }

        return new SubmitEmployeeHolidaySelectionsResponse(
                true,
                "Holiday selections submitted successfully",
                createdSelections,
                createdSelections.size()
        );
    }

    /**
     * Check if holiday is applicable to employee based on company/location restrictions.
     */
    private boolean isHolidayApplicableToEmployee(Holiday holiday, Long employeeCompanyId, Long employeeLocationId) {
        // If holiday has no company mappings, it's global (applicable to all)
        boolean companyApplicable = true;
        if (holiday.getCompanyMappings() != null && !holiday.getCompanyMappings().isEmpty()) {
            // Holiday is restricted to specific companies
            if (employeeCompanyId == null) {
                companyApplicable = false;
            } else {
                companyApplicable = holiday.getCompanyMappings().stream()
                        .anyMatch(mapping -> mapping.getCompany().getId().equals(employeeCompanyId));
            }
        }

        // If holiday has no location mappings, it's global (applicable to all)
        boolean locationApplicable = true;
        if (holiday.getLocationMappings() != null && !holiday.getLocationMappings().isEmpty()) {
            // Holiday is restricted to specific locations
            if (employeeLocationId == null) {
                locationApplicable = false;
            } else {
                locationApplicable = holiday.getLocationMappings().stream()
                        .anyMatch(mapping -> mapping.getLocation().getId().equals(employeeLocationId));
            }
        }

        return companyApplicable && locationApplicable;
    }

    /**
     * Parse financial year string to extract year.
     * Supports formats: "2025-26" -> 2025, "FY2026" -> 2026, "2025" -> 2025
     */
    private Integer parseFinancialYear(String financialYear) {
        if (financialYear == null || financialYear.isBlank()) {
            throw new IllegalArgumentException("Financial year cannot be null or empty");
        }

        // Handle "2025-26" format
        if (financialYear.contains("-")) {
            return Integer.parseInt(financialYear.split("-")[0]);
        }

        // Handle "FY2026" format
        if (financialYear.toUpperCase().startsWith("FY")) {
            return Integer.parseInt(financialYear.substring(2));
        }

        // Handle "2025" format
        try {
            return Integer.parseInt(financialYear);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid financial year format: " + financialYear);
        }
    }
}
