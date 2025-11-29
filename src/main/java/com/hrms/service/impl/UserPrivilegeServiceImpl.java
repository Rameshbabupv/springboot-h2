package com.hrms.service.impl;

import com.hrms.dto.request.OrganizationalScopeRequest;
import com.hrms.dto.request.UserPrivilegeRequest;
import com.hrms.dto.response.HrmsModuleResponse;
import com.hrms.dto.response.UserOrganizationalScopeResponse;
import com.hrms.dto.response.UserPrivilegeResponse;
import com.hrms.entity.HrmsModule;
import com.hrms.entity.UserOrganizationalScope;
import com.hrms.entity.UserPrivilege;
import com.hrms.mapper.UserPrivilegeMapper;
import com.hrms.repository.HrmsModuleRepository;
import com.hrms.repository.UserOrganizationalScopeRepository;
import com.hrms.repository.UserPrivilegeRepository;
import com.hrms.service.UserPrivilegeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of UserPrivilegeService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPrivilegeServiceImpl implements UserPrivilegeService {

    private final HrmsModuleRepository hrmsModuleRepository;
    private final UserPrivilegeRepository userPrivilegeRepository;
    private final UserOrganizationalScopeRepository organizationalScopeRepository;
    private final UserPrivilegeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<HrmsModuleResponse> getHrmsModules(String tenantId) {
        log.debug("Getting HRMS modules for tenant: {}", tenantId);
        List<HrmsModule> modules = hrmsModuleRepository.findByTenantIdAndIsActiveTrueOrderByDisplayOrder(tenantId);
        return modules.stream()
                .map(mapper::toHrmsModuleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HrmsModuleResponse> getHrmsModulesByCategory(String tenantId, String category) {
        log.debug("Getting HRMS modules for tenant: {} and category: {}", tenantId, category);
        List<HrmsModule> modules = hrmsModuleRepository
                .findByTenantIdAndModuleCategoryAndIsActiveTrueOrderByDisplayOrder(tenantId, category);
        return modules.stream()
                .map(mapper::toHrmsModuleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPrivilegeResponse> getUserPrivileges(String tenantId, Long userId) {
        log.debug("Getting privileges for tenant: {} and user: {}", tenantId, userId);
        List<UserPrivilege> privileges = userPrivilegeRepository.findByTenantIdAndUserId(tenantId, userId);
        return privileges.stream()
                .map(mapper::toUserPrivilegeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<UserPrivilegeResponse> saveUserPrivileges(String tenantId, Long userId,
                                                           List<UserPrivilegeRequest> privileges,
                                                           String currentUser) {
        log.debug("Saving {} privileges for tenant: {} and user: {}", privileges.size(), tenantId, userId);

        List<UserPrivilege> savedPrivileges = privileges.stream()
                .map(request -> {
                    // Check if privilege already exists
                    Optional<UserPrivilege> existingPrivilege = userPrivilegeRepository
                            .findByTenantIdAndUserIdAndModuleCode(tenantId, userId, request.getModuleCode());

                    UserPrivilege privilege;
                    if (existingPrivilege.isPresent()) {
                        // Update existing privilege
                        privilege = existingPrivilege.get();
                        mapper.updateUserPrivilegeFromRequest(request, privilege);
                        privilege.setUpdatedBy(currentUser);
                    } else {
                        // Create new privilege
                        privilege = mapper.toUserPrivilege(request);
                        privilege.setTenantId(tenantId);
                        privilege.setUserId(userId);
                        privilege.setCreatedBy(currentUser);
                        privilege.setUpdatedBy(currentUser);
                    }

                    return userPrivilegeRepository.save(privilege);
                })
                .collect(Collectors.toList());

        log.debug("Successfully saved {} privileges", savedPrivileges.size());
        return savedPrivileges.stream()
                .map(mapper::toUserPrivilegeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteUserPrivileges(String tenantId, Long userId) {
        log.debug("Deleting all privileges for tenant: {} and user: {}", tenantId, userId);
        try {
            userPrivilegeRepository.deleteByTenantIdAndUserId(tenantId, userId);
            log.debug("Successfully deleted privileges for user: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting privileges for user: {}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserOrganizationalScopeResponse> getUserOrganizationalScope(String tenantId, Long userId) {
        log.debug("Getting organizational scope for tenant: {} and user: {}", tenantId, userId);
        List<UserOrganizationalScope> scopes = organizationalScopeRepository.findByTenantIdAndUserId(tenantId, userId);
        return scopes.stream()
                .map(mapper::toOrganizationalScopeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<UserOrganizationalScopeResponse> saveOrganizationalScope(String tenantId, Long userId,
                                                                          List<OrganizationalScopeRequest> scopes,
                                                                          String currentUser) {
        log.debug("Saving {} organizational scopes for tenant: {} and user: {}", scopes.size(), tenantId, userId);

        // Delete existing scopes for this user
        organizationalScopeRepository.deleteByTenantIdAndUserId(tenantId, userId);

        // Create new scopes
        List<UserOrganizationalScope> savedScopes = scopes.stream()
                .map(request -> {
                    UserOrganizationalScope scope = mapper.toOrganizationalScope(request);
                    scope.setTenantId(tenantId);
                    scope.setUserId(userId);
                    scope.setCreatedBy(currentUser);
                    return organizationalScopeRepository.save(scope);
                })
                .collect(Collectors.toList());

        log.debug("Successfully saved {} organizational scopes", savedScopes.size());
        return savedScopes.stream()
                .map(mapper::toOrganizationalScopeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteOrganizationalScope(String tenantId, Long userId) {
        log.debug("Deleting organizational scope for tenant: {} and user: {}", tenantId, userId);
        try {
            organizationalScopeRepository.deleteByTenantIdAndUserId(tenantId, userId);
            log.debug("Successfully deleted organizational scope for user: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting organizational scope for user: {}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(String tenantId, Long userId, String moduleCode) {
        return userPrivilegeRepository.hasAnyPermission(tenantId, userId, moduleCode);
    }
}
