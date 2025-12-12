package com.hrms.service;

import com.hrms.dto.response.EmployeeHolidaySelectionsResponse;
import com.hrms.dto.response.SubmitEmployeeHolidaySelectionsResponse;
import com.hrms.entity.Holiday;

import java.util.List;

/**
 * Service for managing employee optional holiday selections.
 */
public interface EmployeeHolidaySelectionService {

    /**
     * Get available optional holidays for employee selection.
     * Filters by Type D/OPTIONAL category and applies company/location restrictions.
     */
    List<Holiday> getAvailableOptionalHolidays(String tenantId, String financialYear, Long employeeId);

    /**
     * Get employee's holiday selections for a financial year.
     */
    EmployeeHolidaySelectionsResponse getEmployeeHolidaySelections(
            String tenantId, Long employeeId, String financialYear);

    /**
     * Submit employee's final holiday selections.
     * Validates count, deadline, holiday types, and replaces existing selections.
     */
    SubmitEmployeeHolidaySelectionsResponse submitEmployeeHolidaySelections(
            String tenantId, Long employeeId, String financialYear, List<Long> holidayIds);
}
