package com.hrms.resolver;

import com.hrms.dto.request.OrganizationalScopeRequest;
import com.hrms.dto.request.UserPrivilegeRequest;
import com.hrms.dto.response.HrmsModuleResponse;
import com.hrms.dto.response.UserOrganizationalScopeResponse;
import com.hrms.dto.response.UserPrivilegeResponse;
import com.hrms.service.UserPrivilegeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL Resolver for User Privilege Management
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserPrivilegeResolver {

    private final UserPrivilegeService userPrivilegeService;

    /**
     * Query: Get all HRMS modules
     */
    @QueryMapping
    public List<HrmsModuleResponse> hrmsModules(@Argument String tenantId) {
        log.debug("GraphQL Query: hrmsModules for tenant: {}", tenantId);
        return userPrivilegeService.getHrmsModules(tenantId);
    }

    /**
     * Query: Get HRMS modules by category
     */
    @QueryMapping
    public List<HrmsModuleResponse> hrmsModulesByCategory(@Argument String tenantId, @Argument String category) {
        log.debug("GraphQL Query: hrmsModulesByCategory for tenant: {} and category: {}", tenantId, category);
        return userPrivilegeService.getHrmsModulesByCategory(tenantId, category);
    }

    /**
     * Query: Get user privileges
     */
    @QueryMapping
    public List<UserPrivilegeResponse> userPrivileges(@Argument String tenantId, @Argument Long userId) {
        log.debug("GraphQL Query: userPrivileges for tenant: {} and user: {}", tenantId, userId);
        return userPrivilegeService.getUserPrivileges(tenantId, userId);
    }

    /**
     * Query: Get user organizational scope
     */
    @QueryMapping
    public List<UserOrganizationalScopeResponse> userOrganizationalScope(@Argument String tenantId, @Argument Long userId) {
        log.debug("GraphQL Query: userOrganizationalScope for tenant: {} and user: {}", tenantId, userId);
        return userPrivilegeService.getUserOrganizationalScope(tenantId, userId);
    }

    /**
     * Query: Check if user has any permission for a module
     */
    @QueryMapping
    public Boolean hasAnyPermission(@Argument String tenantId, @Argument Long userId, @Argument String moduleCode) {
        log.debug("GraphQL Query: hasAnyPermission for tenant: {}, user: {}, module: {}", tenantId, userId, moduleCode);
        return userPrivilegeService.hasAnyPermission(tenantId, userId, moduleCode);
    }

    /**
     * Mutation: Save user privileges
     */
    @MutationMapping
    public List<UserPrivilegeResponse> saveUserPrivileges(
            @Argument String tenantId,
            @Argument Long userId,
            @Argument List<UserPrivilegeRequest> privileges) {
        log.debug("GraphQL Mutation: saveUserPrivileges for tenant: {} and user: {}", tenantId, userId);
        String currentUser = getCurrentUsername();
        return userPrivilegeService.saveUserPrivileges(tenantId, userId, privileges, currentUser);
    }

    /**
     * Mutation: Delete user privileges
     */
    @MutationMapping
    public Boolean deleteUserPrivileges(@Argument String tenantId, @Argument Long userId) {
        log.debug("GraphQL Mutation: deleteUserPrivileges for tenant: {} and user: {}", tenantId, userId);
        return userPrivilegeService.deleteUserPrivileges(tenantId, userId);
    }

    /**
     * Mutation: Save organizational scope
     */
    @MutationMapping
    public List<UserOrganizationalScopeResponse> saveOrganizationalScope(
            @Argument String tenantId,
            @Argument Long userId,
            @Argument List<OrganizationalScopeRequest> scopes) {
        log.debug("GraphQL Mutation: saveOrganizationalScope for tenant: {} and user: {}", tenantId, userId);
        String currentUser = getCurrentUsername();
        return userPrivilegeService.saveOrganizationalScope(tenantId, userId, scopes, currentUser);
    }

    /**
     * Mutation: Delete organizational scope
     */
    @MutationMapping
    public Boolean deleteOrganizationalScope(@Argument String tenantId, @Argument Long userId) {
        log.debug("GraphQL Mutation: deleteOrganizationalScope for tenant: {} and user: {}", tenantId, userId);
        return userPrivilegeService.deleteOrganizationalScope(tenantId, userId);
    }

    /**
     * Helper method to get current username from security context
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}
