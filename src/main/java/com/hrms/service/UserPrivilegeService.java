package com.hrms.service;

import com.hrms.dto.request.OrganizationalScopeRequest;
import com.hrms.dto.request.UserPrivilegeRequest;
import com.hrms.dto.response.HrmsModuleResponse;
import com.hrms.dto.response.UserOrganizationalScopeResponse;
import com.hrms.dto.response.UserPrivilegeResponse;

import java.util.List;

/**
 * Service interface for User Privilege Management
 */
public interface UserPrivilegeService {

    /**
     * Get all HRMS modules for a tenant
     */
    List<HrmsModuleResponse> getHrmsModules(String tenantId);

    /**
     * Get HRMS modules by category
     */
    List<HrmsModuleResponse> getHrmsModulesByCategory(String tenantId, String category);

    /**
     * Get all privileges for a user
     */
    List<UserPrivilegeResponse> getUserPrivileges(String tenantId, Long userId);

    /**
     * Save/update user privileges (bulk operation)
     */
    List<UserPrivilegeResponse> saveUserPrivileges(String tenantId, Long userId,
                                                    List<UserPrivilegeRequest> privileges,
                                                    String currentUser);

    /**
     * Delete all privileges for a user
     */
    boolean deleteUserPrivileges(String tenantId, Long userId);

    /**
     * Get organizational scope for a user
     */
    List<UserOrganizationalScopeResponse> getUserOrganizationalScope(String tenantId, Long userId);

    /**
     * Save organizational scope for a user (bulk operation)
     */
    List<UserOrganizationalScopeResponse> saveOrganizationalScope(String tenantId, Long userId,
                                                                   List<OrganizationalScopeRequest> scopes,
                                                                   String currentUser);

    /**
     * Delete organizational scope for a user
     */
    boolean deleteOrganizationalScope(String tenantId, Long userId);

    /**
     * Check if user has any permission for a module
     */
    boolean hasAnyPermission(String tenantId, Long userId, String moduleCode);
}
