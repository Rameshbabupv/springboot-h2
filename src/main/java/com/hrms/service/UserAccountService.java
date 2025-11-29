package com.hrms.service;

import com.hrms.dto.request.ChangePasswordRequest;
import com.hrms.dto.request.UserAccountRequest;
import com.hrms.dto.request.UserAccountUpdateRequest;
import com.hrms.dto.response.UserAccountResponse;

import java.util.List;

/**
 * Service interface for User Account operations
 */
public interface UserAccountService {

    /**
     * Create a new user account
     */
    UserAccountResponse createUser(UserAccountRequest request);

    /**
     * Update user account
     */
    UserAccountResponse updateUser(Long id, UserAccountUpdateRequest request);

    /**
     * Delete user account (soft delete)
     */
    boolean deleteUser(Long id);

    /**
     * Get user account by ID
     */
    UserAccountResponse getUserById(Long id);

    /**
     * Get user account by username and tenant
     */
    UserAccountResponse getUserByUsername(String tenantId, String username);

    /**
     * Get user account by employee ID
     */
    UserAccountResponse getUserByEmployeeId(Long employeeId);

    /**
     * Get all users for a tenant
     */
    List<UserAccountResponse> getUsersByTenant(String tenantId);

    /**
     * Get users by role
     */
    List<UserAccountResponse> getUsersByRole(String tenantId, String role);

    /**
     * Get active users
     */
    List<UserAccountResponse> getActiveUsers(String tenantId);

    /**
     * Activate user account
     */
    UserAccountResponse activateUser(Long id);

    /**
     * Deactivate user account
     */
    UserAccountResponse deactivateUser(Long id);

    /**
     * Lock user account
     */
    UserAccountResponse lockUser(Long id, String reason);

    /**
     * Unlock user account
     */
    UserAccountResponse unlockUser(Long id);

    /**
     * Change password
     */
    boolean changePassword(ChangePasswordRequest request);

    /**
     * Force password change on next login
     */
    boolean forcePasswordChange(Long userId);

    /**
     * Check if username is available
     */
    boolean isUsernameAvailable(String tenantId, String username);

    /**
     * Increment failed login attempts
     */
    void incrementFailedLoginAttempts(Long userId);

    /**
     * Reset failed login attempts
     */
    void resetFailedLoginAttempts(Long userId);

    /**
     * Update last login info
     */
    void updateLastLogin(Long userId, String ipAddress);
}
