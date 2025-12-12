package com.hrms.graphql.resolver;

import com.hrms.dto.response.EmployeeHolidaySelectionsResponse;
import com.hrms.dto.response.SubmitEmployeeHolidaySelectionsResponse;
import com.hrms.entity.Employee;
import com.hrms.entity.EmployeeHolidaySelection;
import com.hrms.entity.Holiday;
import com.hrms.service.EmployeeHolidaySelectionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GraphQL resolver for Employee Holiday Selection operations.
 */
@Controller
public class EmployeeHolidaySelectionResolver {

    private final EmployeeHolidaySelectionService holidaySelectionService;

    public EmployeeHolidaySelectionResolver(EmployeeHolidaySelectionService holidaySelectionService) {
        this.holidaySelectionService = holidaySelectionService;
    }

    // Queries

    @QueryMapping
    public List<Holiday> getAvailableOptionalHolidays(@Argument String tenantId,
                                                       @Argument String financialYear,
                                                       @Argument Long employeeId) {
        return holidaySelectionService.getAvailableOptionalHolidays(tenantId, financialYear, employeeId);
    }

    @QueryMapping
    public EmployeeHolidaySelectionsResponse getEmployeeHolidaySelections(
            @Argument String tenantId,
            @Argument Long employeeId,
            @Argument String financialYear) {
        return holidaySelectionService.getEmployeeHolidaySelections(tenantId, employeeId, financialYear);
    }

    // Mutations

    @MutationMapping
    public SubmitEmployeeHolidaySelectionsResponse submitEmployeeHolidaySelections(
            @Argument String tenantId,
            @Argument Long employeeId,
            @Argument String financialYear,
            @Argument List<Long> holidayIds) {
        return holidaySelectionService.submitEmployeeHolidaySelections(
                tenantId, employeeId, financialYear, holidayIds);
    }

    // Field Resolvers

    /**
     * Resolve employeeId field from employee entity.
     */
    @SchemaMapping(typeName = "EmployeeHolidaySelection", field = "employeeId")
    public Long employeeId(EmployeeHolidaySelection selection) {
        return selection.getEmployee() != null ? selection.getEmployee().getId() : null;
    }

    /**
     * Resolve holidayId field from holiday entity.
     */
    @SchemaMapping(typeName = "EmployeeHolidaySelection", field = "holidayId")
    public Long holidayId(EmployeeHolidaySelection selection) {
        return selection.getHoliday() != null ? selection.getHoliday().getId() : null;
    }

    /**
     * Resolve selectedAt field as ISO timestamp string.
     */
    @SchemaMapping(typeName = "EmployeeHolidaySelection", field = "selectedAt")
    public String selectedAt(EmployeeHolidaySelection selection) {
        if (selection.getSelectedAt() == null) {
            return null;
        }
        return selection.getSelectedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Resolve submittedAt field as ISO timestamp string.
     */
    @SchemaMapping(typeName = "EmployeeHolidaySelection", field = "submittedAt")
    public String submittedAt(EmployeeHolidaySelection selection) {
        if (selection.getSubmittedAt() == null) {
            return null;
        }
        return selection.getSubmittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Resolve submittedAt in EmployeeHolidaySelectionsResponse.
     */
    @SchemaMapping(typeName = "EmployeeHolidaySelectionsResponse", field = "submittedAt")
    public String submittedAtResponse(EmployeeHolidaySelectionsResponse response) {
        if (response.getSubmittedAt() == null) {
            return null;
        }
        return response.getSubmittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
