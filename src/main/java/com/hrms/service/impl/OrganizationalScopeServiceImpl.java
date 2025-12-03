package com.hrms.service.impl;

import com.hrms.dto.request.EmployeeFilterCriteria;
import com.hrms.dto.request.OrganizationalScopeDTO;
import com.hrms.entity.UserOrganizationalScope;
import com.hrms.exception.UnauthorizedException;
import com.hrms.repository.UserOrganizationalScopeRepository;
import com.hrms.service.OrganizationalScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Organizational Scope Service
 * Handles security filtering based on user's organizational boundaries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationalScopeServiceImpl implements OrganizationalScopeService {

    private final UserOrganizationalScopeRepository scopeRepository;

    @Override
    public OrganizationalScopeDTO getUserScope(String tenantId, Long userId) {
        log.debug("Fetching organizational scope for tenant: {}, user: {}", tenantId, userId);

        // Fetch all scope records for this user
        List<UserOrganizationalScope> scopes = scopeRepository.findByTenantIdAndUserId(tenantId, userId);

        OrganizationalScopeDTO scopeDTO = new OrganizationalScopeDTO();
        scopeDTO.setTenantId(tenantId);
        scopeDTO.setUserId(userId);

        // Group scope IDs by type
        for (UserOrganizationalScope scope : scopes) {
            switch (scope.getScopeType()) {
                case UserOrganizationalScope.SCOPE_COMPANY:
                    scopeDTO.getCompanyIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_LOCATION:
                    scopeDTO.getLocationIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_DIVISION:
                    scopeDTO.getDivisionIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_DEPARTMENT:
                    scopeDTO.getDepartmentIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_SECTION:
                    scopeDTO.getSectionIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_DESIGNATION:
                    scopeDTO.getDesignationIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_GRADE:
                    scopeDTO.getGradeIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_JOB_FUNCTION:
                    scopeDTO.getJobFunctionIds().add(scope.getScopeId());
                    break;
                case UserOrganizationalScope.SCOPE_EMPLOYMENT_TYPE:
                    scopeDTO.getEmploymentTypeIds().add(scope.getScopeId());
                    break;
                default:
                    log.warn("Unknown scope type: {}", scope.getScopeType());
            }
        }

        log.debug("User scope: {} companies, {} locations, {} divisions, {} departments, {} sections",
            scopeDTO.getCompanyIds().size(),
            scopeDTO.getLocationIds().size(),
            scopeDTO.getDivisionIds().size(),
            scopeDTO.getDepartmentIds().size(),
            scopeDTO.getSectionIds().size());

        return scopeDTO;
    }

    @Override
    public void validateAccess(OrganizationalScopeDTO userScope, EmployeeFilterCriteria requestedFilters) {
        log.debug("Validating access for user: {}", userScope.getUserId());

        // If user has no restrictions (super admin), allow all access
        if (userScope.isEmpty()) {
            log.debug("User has no restrictions - full access granted");
            return;
        }

        // Validate each filter type
        validateFilterAccess("Company", requestedFilters.getCompanyIds(), userScope.getCompanyIds());
        validateFilterAccess("Location", requestedFilters.getLocationIds(), userScope.getLocationIds());
        validateFilterAccess("Division", requestedFilters.getDivisionIds(), userScope.getDivisionIds());
        validateFilterAccess("Department", requestedFilters.getDepartmentIds(), userScope.getDepartmentIds());
        validateFilterAccess("Section", requestedFilters.getSectionIds(), userScope.getSectionIds());
        validateFilterAccess("Designation", requestedFilters.getDesignationIds(), userScope.getDesignationIds());
        validateFilterAccess("Grade", requestedFilters.getGradeIds(), userScope.getGradeIds());
        validateFilterAccess("JobFunction", requestedFilters.getJobFunctionIds(), userScope.getJobFunctionIds());
        validateFilterAccess("EmploymentType", requestedFilters.getEmploymentTypeIds(), userScope.getEmploymentTypeIds());

        log.debug("Access validation passed");
    }

    private void validateFilterAccess(String filterName, List<Long> requestedIds, List<Long> allowedIds) {
        if (requestedIds == null || requestedIds.isEmpty()) {
            return; // No filter requested
        }

        if (allowedIds.isEmpty()) {
            return; // User has no restrictions on this filter type
        }

        // Check if all requested IDs are in allowed list
        for (Long requestedId : requestedIds) {
            if (!allowedIds.contains(requestedId)) {
                String message = String.format("Access denied: User not authorized to access %s ID: %d",
                    filterName, requestedId);
                log.warn(message);
                throw new UnauthorizedException(message);
            }
        }
    }

    @Override
    public List<Long> mergeFilters(List<Long> requestedIds, List<Long> allowedIds) {
        // If no restriction on allowed IDs, return requested IDs as-is
        if (allowedIds == null || allowedIds.isEmpty()) {
            return requestedIds != null ? requestedIds : new ArrayList<>();
        }

        // If no specific IDs requested, return all allowed IDs
        if (requestedIds == null || requestedIds.isEmpty()) {
            return new ArrayList<>(allowedIds);
        }

        // Return intersection (only IDs that exist in both lists)
        return requestedIds.stream()
            .filter(allowedIds::contains)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public EmployeeFilterCriteria applySecurityScope(String tenantId, Long userId, EmployeeFilterCriteria criteria) {
        log.debug("Applying security scope for tenant: {}, user: {}", tenantId, userId);

        // Get user's organizational scope
        OrganizationalScopeDTO userScope = getUserScope(tenantId, userId);

        // If user has no restrictions, return criteria as-is
        if (userScope.isEmpty()) {
            log.debug("User has full access - no scope restrictions applied");
            return criteria;
        }

        // Validate access first (throws exception if unauthorized)
        validateAccess(userScope, criteria);

        // Merge filters (intersection of requested and allowed)
        criteria.setCompanyIds(mergeFilters(criteria.getCompanyIds(), userScope.getCompanyIds()));
        criteria.setLocationIds(mergeFilters(criteria.getLocationIds(), userScope.getLocationIds()));
        criteria.setDivisionIds(mergeFilters(criteria.getDivisionIds(), userScope.getDivisionIds()));
        criteria.setDepartmentIds(mergeFilters(criteria.getDepartmentIds(), userScope.getDepartmentIds()));
        criteria.setSectionIds(mergeFilters(criteria.getSectionIds(), userScope.getSectionIds()));
        criteria.setDesignationIds(mergeFilters(criteria.getDesignationIds(), userScope.getDesignationIds()));
        criteria.setGradeIds(mergeFilters(criteria.getGradeIds(), userScope.getGradeIds()));
        criteria.setJobFunctionIds(mergeFilters(criteria.getJobFunctionIds(), userScope.getJobFunctionIds()));
        criteria.setEmploymentTypeIds(mergeFilters(criteria.getEmploymentTypeIds(), userScope.getEmploymentTypeIds()));

        log.debug("Security scope applied successfully");
        return criteria;
    }
}
