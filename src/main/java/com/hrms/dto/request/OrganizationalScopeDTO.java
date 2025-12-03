package com.hrms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for user's organizational scope
 * Contains all allowed organizational boundaries for filtering
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationalScopeDTO {

    private String tenantId;
    private Long userId;

    // All 9 organizational parameters
    private List<Long> companyIds = new ArrayList<>();
    private List<Long> locationIds = new ArrayList<>();
    private List<Long> divisionIds = new ArrayList<>();
    private List<Long> departmentIds = new ArrayList<>();
    private List<Long> sectionIds = new ArrayList<>();
    private List<Long> designationIds = new ArrayList<>();
    private List<Long> gradeIds = new ArrayList<>();
    private List<Long> jobFunctionIds = new ArrayList<>();
    private List<Long> employmentTypeIds = new ArrayList<>();

    /**
     * Check if scope is empty (no restrictions)
     */
    public boolean isEmpty() {
        return companyIds.isEmpty()
            && locationIds.isEmpty()
            && divisionIds.isEmpty()
            && departmentIds.isEmpty()
            && sectionIds.isEmpty()
            && designationIds.isEmpty()
            && gradeIds.isEmpty()
            && jobFunctionIds.isEmpty()
            && employmentTypeIds.isEmpty();
    }

    /**
     * Check if a specific scope type has restrictions
     */
    public boolean hasCompanyRestriction() {
        return !companyIds.isEmpty();
    }

    public boolean hasLocationRestriction() {
        return !locationIds.isEmpty();
    }

    public boolean hasDivisionRestriction() {
        return !divisionIds.isEmpty();
    }

    public boolean hasDepartmentRestriction() {
        return !departmentIds.isEmpty();
    }

    public boolean hasSectionRestriction() {
        return !sectionIds.isEmpty();
    }

    public boolean hasDesignationRestriction() {
        return !designationIds.isEmpty();
    }

    public boolean hasGradeRestriction() {
        return !gradeIds.isEmpty();
    }

    public boolean hasJobFunctionRestriction() {
        return !jobFunctionIds.isEmpty();
    }

    public boolean hasEmploymentTypeRestriction() {
        return !employmentTypeIds.isEmpty();
    }
}
