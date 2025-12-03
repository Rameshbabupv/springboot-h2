package com.hrms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Employee Filter Criteria DTO
 * Contains all filtering parameters for employee queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFilterCriteria {

    // Tenant context
    private String tenantId;

    // 9 Organizational filters
    private List<Long> companyIds;
    private List<Long> locationIds;
    private List<Long> divisionIds;
    private List<Long> departmentIds;
    private List<Long> sectionIds;
    private List<Long> designationIds;
    private List<Long> gradeIds;
    private List<Long> jobFunctionIds;
    private List<Long> employmentTypeIds;

    // Additional filters
    private String searchQuery; // For employee name/emp_id search
    private String employeeStatus; // Active, Inactive, etc.
    private Long reportingManagerId;

    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection; // ASC or DESC

    /**
     * Check if any organizational filter is applied
     */
    public boolean hasOrganizationalFilters() {
        return hasCompanyFilter()
            || hasLocationFilter()
            || hasDivisionFilter()
            || hasDepartmentFilter()
            || hasSectionFilter()
            || hasDesignationFilter()
            || hasGradeFilter()
            || hasJobFunctionFilter()
            || hasEmploymentTypeFilter();
    }

    public boolean hasCompanyFilter() {
        return companyIds != null && !companyIds.isEmpty();
    }

    public boolean hasLocationFilter() {
        return locationIds != null && !locationIds.isEmpty();
    }

    public boolean hasDivisionFilter() {
        return divisionIds != null && !divisionIds.isEmpty();
    }

    public boolean hasDepartmentFilter() {
        return departmentIds != null && !departmentIds.isEmpty();
    }

    public boolean hasSectionFilter() {
        return sectionIds != null && !sectionIds.isEmpty();
    }

    public boolean hasDesignationFilter() {
        return designationIds != null && !designationIds.isEmpty();
    }

    public boolean hasGradeFilter() {
        return gradeIds != null && !gradeIds.isEmpty();
    }

    public boolean hasJobFunctionFilter() {
        return jobFunctionIds != null && !jobFunctionIds.isEmpty();
    }

    public boolean hasEmploymentTypeFilter() {
        return employmentTypeIds != null && !employmentTypeIds.isEmpty();
    }

    public boolean hasSearchQuery() {
        return searchQuery != null && !searchQuery.trim().isEmpty();
    }

    public boolean hasEmployeeStatusFilter() {
        return employeeStatus != null && !employeeStatus.trim().isEmpty();
    }

    public boolean hasReportingManagerFilter() {
        return reportingManagerId != null;
    }
}
