package com.hrms.graphql.resolver;

import com.hrms.dto.request.*;
import com.hrms.dto.response.*;
import com.hrms.graphql.input.*;
import com.hrms.service.AuthenticationService;
import com.hrms.service.UserAccountService;
import com.hrms.service.UserActivityLogService;
import com.hrms.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GraphQL Resolver for User Management Operations
 * Handles Authentication, User CRUD, Sessions, and Activity Logs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class UserAccountResolver {

    private final UserAccountService userAccountService;
    private final AuthenticationService authenticationService;
    private final UserSessionService sessionService;
    private final UserActivityLogService activityLogService;

    // =====================================================
    // QUERY OPERATIONS - User Accounts
    // =====================================================

    @QueryMapping
    public List<UserAccountResponse> userAccounts(@Argument String tenantId) {
        log.debug("GraphQL Query: userAccounts - tenantId: {}", tenantId);
        List<UserAccountResponse> result = userAccountService.getUsersByTenant(tenantId);
        log.info("GraphQL Response: userAccounts - returned {} user accounts for tenant: {}", result.size(), tenantId);
        return result;
    }

    @QueryMapping
    public UserAccountResponse userAccount(@Argument String id) {
        log.debug("GraphQL Query: userAccount - id: {}", id);
        UserAccountResponse result = userAccountService.getUserById(Long.parseLong(id));
        log.info("GraphQL Response: userAccount - returned user: {}", result.getUsername());
        return result;
    }

    @QueryMapping
    public UserAccountResponse userAccountByUsername(@Argument String tenantId, @Argument String username) {
        log.debug("GraphQL Query: userAccountByUsername - tenantId: {}, username: {}", tenantId, username);
        UserAccountResponse result = userAccountService.getUserByUsername(tenantId, username);
        log.info("GraphQL Response: userAccountByUsername - returned user: {}", result.getUsername());
        return result;
    }

    @QueryMapping
    public UserAccountResponse userAccountByEmployeeId(@Argument String employeeId) {
        log.debug("GraphQL Query: userAccountByEmployeeId - employeeId: {}", employeeId);
        UserAccountResponse result = userAccountService.getUserByEmployeeId(Long.parseLong(employeeId));
        log.info("GraphQL Response: userAccountByEmployeeId - returned user: {}", result.getUsername());
        return result;
    }

    @QueryMapping
    public List<UserAccountResponse> userAccountsByRole(@Argument String tenantId, @Argument String role) {
        log.debug("GraphQL Query: userAccountsByRole - tenantId: {}, role: {}", tenantId, role);
        List<UserAccountResponse> result = userAccountService.getUsersByRole(tenantId, role);
        log.info("GraphQL Response: userAccountsByRole - returned {} users with role: {}", result.size(), role);
        return result;
    }

    @QueryMapping
    public List<UserAccountResponse> activeUserAccounts(@Argument String tenantId) {
        log.debug("GraphQL Query: activeUserAccounts - tenantId: {}", tenantId);
        List<UserAccountResponse> result = userAccountService.getActiveUsers(tenantId);
        log.info("GraphQL Response: activeUserAccounts - returned {} active users", result.size());
        return result;
    }

    @QueryMapping
    public Boolean isUsernameAvailable(@Argument String tenantId, @Argument String username) {
        log.debug("GraphQL Query: isUsernameAvailable - tenantId: {}, username: {}", tenantId, username);
        Boolean result = userAccountService.isUsernameAvailable(tenantId, username);
        log.info("GraphQL Response: isUsernameAvailable - username '{}' available: {}", username, result);
        return result;
    }

    // =====================================================
    // QUERY OPERATIONS - Sessions
    // =====================================================

    @QueryMapping
    public List<UserSessionResponse> userSessions(@Argument String userId) {
        log.debug("GraphQL Query: userSessions - userId: {}", userId);
        return sessionService.getUserSessions(Long.parseLong(userId));
    }

    @QueryMapping
    public List<UserSessionResponse> activeSessions(@Argument String userId) {
        log.debug("GraphQL Query: activeSessions - userId: {}", userId);
        return sessionService.getActiveSessions(Long.parseLong(userId));
    }

    // =====================================================
    // QUERY OPERATIONS - Activity Logs
    // =====================================================

    @QueryMapping
    public List<UserActivityLogResponse> userActivityLogs(
            @Argument String userId,
            @Argument String actionType,
            @Argument LocalDateTime startDate,
            @Argument LocalDateTime endDate,
            @Argument Integer limit,
            @Argument Integer offset) {

        log.debug("GraphQL Query: userActivityLogs - userId: {}, actionType: {}", userId, actionType);

        Long userIdLong = userId != null ? Long.parseLong(userId) : null;
        int limitValue = limit != null ? limit : 100;
        int offsetValue = offset != null ? offset : 0;

        return activityLogService.searchActivityLogs(userIdLong, actionType, startDate, endDate, limitValue, offsetValue);
    }

    // =====================================================
    // MUTATION OPERATIONS - Authentication
    // =====================================================

    @MutationMapping
    public AuthResponse login(@Argument LoginInput input) {
        log.info("GraphQL Mutation: login - username: {}, tenant: {}", input.getUsername(), input.getTenantId());

        LoginRequest request = new LoginRequest(
            input.getTenantId(),
            input.getUsername(),
            input.getPassword(),
            input.getDeviceInfo()
        );

        return authenticationService.login(request, "GraphQL", "GraphQL Client");
    }

    @MutationMapping
    public Boolean logout(@Argument String sessionToken) {
        log.info("GraphQL Mutation: logout");
        return authenticationService.logout(sessionToken);
    }

    @MutationMapping
    public AuthResponse refreshToken(@Argument String refreshToken) {
        log.info("GraphQL Mutation: refreshToken");
        return authenticationService.refreshToken(refreshToken);
    }

    // =====================================================
    // MUTATION OPERATIONS - User Management
    // =====================================================

    @MutationMapping
    public UserAccountResponse createUserAccount(@Argument UserAccountInput input) {
        log.info("GraphQL Mutation: createUserAccount - username: {}", input.getUsername());

        UserAccountRequest request = new UserAccountRequest(
            input.getTenantId(),
            input.getEmployeeId(),
            input.getUsername(),
            input.getEmail(),
            input.getPassword(),
            input.getRole(),
            input.getIsActive(),
            input.getMustChangePassword(),
            input.getInheritFromDesignation()
        );

        return userAccountService.createUser(request);
    }

    @MutationMapping
    public UserAccountResponse updateUserAccount(@Argument String id, @Argument UserAccountUpdateInput input) {
        log.info("GraphQL Mutation: updateUserAccount - id: {}", id);

        UserAccountUpdateRequest request = new UserAccountUpdateRequest(
            input.getUsername(),
            input.getEmail(),
            input.getRole(),
            input.getIsActive(),
            input.getIsLocked(),
            input.getMustChangePassword(),
            input.getInheritFromDesignation()
        );

        return userAccountService.updateUser(Long.parseLong(id), request);
    }

    @MutationMapping
    public Boolean deleteUserAccount(@Argument String id) {
        log.info("GraphQL Mutation: deleteUserAccount - id: {}", id);
        return userAccountService.deleteUser(Long.parseLong(id));
    }

    // =====================================================
    // MUTATION OPERATIONS - Account Status
    // =====================================================

    @MutationMapping
    public UserAccountResponse activateUserAccount(@Argument String id) {
        log.info("GraphQL Mutation: activateUserAccount - id: {}", id);
        return userAccountService.activateUser(Long.parseLong(id));
    }

    @MutationMapping
    public UserAccountResponse deactivateUserAccount(@Argument String id) {
        log.info("GraphQL Mutation: deactivateUserAccount - id: {}", id);
        return userAccountService.deactivateUser(Long.parseLong(id));
    }

    @MutationMapping
    public UserAccountResponse lockUserAccount(@Argument String id, @Argument String reason) {
        log.info("GraphQL Mutation: lockUserAccount - id: {}, reason: {}", id, reason);
        return userAccountService.lockUser(Long.parseLong(id), reason);
    }

    @MutationMapping
    public UserAccountResponse unlockUserAccount(@Argument String id) {
        log.info("GraphQL Mutation: unlockUserAccount - id: {}", id);
        return userAccountService.unlockUser(Long.parseLong(id));
    }

    // =====================================================
    // MUTATION OPERATIONS - Password Management
    // =====================================================

    @MutationMapping
    public Boolean changePassword(@Argument ChangePasswordInput input) {
        log.info("GraphQL Mutation: changePassword - userId: {}", input.getUserId());

        ChangePasswordRequest request = new ChangePasswordRequest(
            input.getUserId(),
            input.getCurrentPassword(),
            input.getNewPassword()
        );

        return userAccountService.changePassword(request);
    }

    @MutationMapping
    public PasswordResetResponse requestPasswordReset(@Argument PasswordResetRequestInput input) {
        log.info("GraphQL Mutation: requestPasswordReset - email: {}", input.getEmail());

        PasswordResetRequestRequest request = new PasswordResetRequestRequest(
            input.getEmail(),
            input.getTenantId()
        );

        return authenticationService.requestPasswordReset(request, "GraphQL");
    }

    @MutationMapping
    public PasswordResetResponse resetPassword(@Argument PasswordResetInput input) {
        log.info("GraphQL Mutation: resetPassword");

        PasswordResetRequest request = new PasswordResetRequest(
            input.getResetToken(),
            input.getNewPassword()
        );

        return authenticationService.resetPassword(request);
    }

    @MutationMapping
    public Boolean forcePasswordChange(@Argument String userId) {
        log.info("GraphQL Mutation: forcePasswordChange - userId: {}", userId);
        return userAccountService.forcePasswordChange(Long.parseLong(userId));
    }

    // =====================================================
    // MUTATION OPERATIONS - Session Management
    // =====================================================

    @MutationMapping
    public Boolean terminateSession(@Argument String sessionId) {
        log.info("GraphQL Mutation: terminateSession - sessionId: {}", sessionId);
        return sessionService.terminateSession(Long.parseLong(sessionId));
    }

    @MutationMapping
    public Boolean terminateAllSessions(@Argument String userId) {
        log.info("GraphQL Mutation: terminateAllSessions - userId: {}", userId);
        return sessionService.terminateAllUserSessions(Long.parseLong(userId));
    }
}
