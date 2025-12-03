package com.hrms.service;

import com.hrms.dto.request.EmployeeFilterCriteria;
import com.hrms.dto.request.OrganizationalScopeDTO;

import java.util.List;

/**
 * Service for managing organizational scope filtering and security
 */
public interface OrganizationalScopeService {

    /**
     * Get user's organizational scope (all allowed companies, locations, etc.)
     *
     * @param tenantId Tenant ID
     * @param userId User ID
     * @return OrganizationalScopeDTO with all allowed organizational boundaries
     */
    OrganizationalScopeDTO getUserScope(String tenantId, Long userId);

    /**
     * Validate if user has access to requested filters
     * Throws exception if user tries to access unauthorized data
     *
     * @param userScope User's allowed scope
     * @param requestedFilters Filters requested by user
     * @throws com.hrms.exception.UnauthorizedException if access denied
     */
    void validateAccess(OrganizationalScopeDTO userScope, EmployeeFilterCriteria requestedFilters);

    /**
     * Merge user's scope with requested filters
     * Returns intersection (only IDs that exist in both lists)
     *
     * @param requestedIds Requested filter IDs (can be null)
     * @param allowedIds User's allowed IDs
     * @return Merged list (intersection)
     */
    List<Long> mergeFilters(List<Long> requestedIds, List<Long> allowedIds);

    /**
     * Apply organizational scope to filter criteria
     * This enforces security by intersecting requested filters with user's allowed scope
     *
     * @param tenantId Tenant ID
     * @param userId User ID
     * @param criteria Filter criteria to apply scope to
     * @return Updated criteria with security applied
     */
    EmployeeFilterCriteria applySecurityScope(String tenantId, Long userId, EmployeeFilterCriteria criteria);
}
