package com.hrms.service.impl;

import com.hrms.dto.request.ChangePasswordRequest;
import com.hrms.dto.request.UserAccountRequest;
import com.hrms.dto.request.UserAccountUpdateRequest;
import com.hrms.dto.response.UserAccountResponse;
import com.hrms.entity.UserAccount;
import com.hrms.entity.UserRole;
import com.hrms.exception.BadRequestException;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.mapper.UserAccountMapper;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.UserAccountRepository;
import com.hrms.service.UserAccountService;
import com.hrms.service.UserActivityLogService;
import com.hrms.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for User Account operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountMapper mapper;
    private final PasswordUtil passwordUtil;
    private final UserActivityLogService activityLogService;

    @Value("${user.password.expiry.days:90}")
    private int passwordExpiryDays;

    @Value("${user.failed.login.threshold:5}")
    private int failedLoginThreshold;

    @Override
    @Transactional
    public UserAccountResponse createUser(UserAccountRequest request) {
        log.info("Creating user account for employee: {}", request.getEmployeeId());

        // Validate employee exists
        if (!employeeRepository.existsById(request.getEmployeeId())) {
            throw new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId());
        }

        // Check if employee already has a user account
        if (userAccountRepository.existsByEmployeeIdAndDeletedAtIsNull(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee already has a user account");
        }

        // Check username uniqueness
        if (userAccountRepository.existsByTenantIdAndUsernameAndDeletedAtIsNull(
                request.getTenantId(), request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Check email uniqueness
        if (userAccountRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(
                request.getTenantId(), request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        // Validate role
        if (!UserRole.isValid(request.getRole())) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        // Map to entity
        UserAccount userAccount = mapper.toEntity(request);

        // Hash password
        String password = request.getPassword();
        if (password == null || password.isEmpty()) {
            password = passwordUtil.generateTemporaryPassword();
            userAccount.setMustChangePassword(true);
            log.info("Generated temporary password for user: {}", request.getUsername());
        }

        if (!passwordUtil.isPasswordValid(password)) {
            throw new BadRequestException("Password does not meet complexity requirements");
        }

        userAccount.setPasswordHash(passwordUtil.hashPassword(password));
        userAccount.setPasswordChangedAt(OffsetDateTime.now());
        userAccount.setPasswordExpiresAt(OffsetDateTime.now().plusDays(passwordExpiryDays));

        // Save user account
        UserAccount savedUser = userAccountRepository.save(userAccount);
        log.info("User account created successfully: {}", savedUser.getUsername());

        // Log activity
        activityLogService.logActivity(
            savedUser.getId(),
            "USER_CREATED",
            "UserAccount",
            savedUser.getId().toString(),
            "User account created for: " + savedUser.getUsername(),
            null, null, null
        );

        return mapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserAccountResponse updateUser(Long id, UserAccountUpdateRequest request) {
        log.info("Updating user account: {}", id);

        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check username uniqueness if changing
        if (request.getUsername() != null && !request.getUsername().equals(userAccount.getUsername())) {
            if (userAccountRepository.existsByTenantIdAndUsernameAndDeletedAtIsNull(
                    userAccount.getTenantId(), request.getUsername())) {
                throw new DuplicateResourceException("Username already exists: " + request.getUsername());
            }
        }

        // Check email uniqueness if changing
        if (request.getEmail() != null && !request.getEmail().equals(userAccount.getEmail())) {
            if (userAccountRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(
                    userAccount.getTenantId(), request.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + request.getEmail());
            }
        }

        // Validate and normalize role if changing
        if (request.getRole() != null) {
            String normalizedRole = UserRole.normalizeRole(request.getRole());
            if (!UserRole.isValid(normalizedRole)) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
            // Update request with normalized role
            request = new UserAccountUpdateRequest(
                request.getUsername(),
                request.getEmail(),
                normalizedRole,
                request.getIsActive(),
                request.getIsLocked(),
                request.getMustChangePassword(),
                request.getInheritFromDesignation()
            );
        }

        // Update entity
        mapper.updateEntityFromRequest(request, userAccount);
        UserAccount updatedUser = userAccountRepository.save(userAccount);

        log.info("User account updated successfully: {}", updatedUser.getUsername());

        // Log activity
        activityLogService.logActivity(
            updatedUser.getId(),
            "USER_UPDATED",
            "UserAccount",
            updatedUser.getId().toString(),
            "User account updated",
            null, null, null
        );

        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user account: {}", id);

        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Soft delete
        userAccount.setDeletedAt(OffsetDateTime.now());
        userAccount.setIsActive(false);
        userAccountRepository.save(userAccount);

        log.info("User account deleted successfully: {}", userAccount.getUsername());

        // Log activity
        activityLogService.logActivity(
            userAccount.getId(),
            "USER_DELETED",
            "UserAccount",
            userAccount.getId().toString(),
            "User account deleted",
            null, null, null
        );

        return true;
    }

    @Override
    public UserAccountResponse getUserById(Long id) {
        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapper.toResponse(userAccount);
    }

    @Override
    public UserAccountResponse getUserByUsername(String tenantId, String username) {
        UserAccount userAccount = userAccountRepository
            .findByTenantIdAndUsernameAndDeletedAtIsNull(tenantId, username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return mapper.toResponse(userAccount);
    }

    @Override
    public UserAccountResponse getUserByEmployeeId(Long employeeId) {
        UserAccount userAccount = userAccountRepository.findByEmployeeIdAndDeletedAtIsNull(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found for employee: " + employeeId));
        return mapper.toResponse(userAccount);
    }

    @Override
    public List<UserAccountResponse> getUsersByTenant(String tenantId) {
        List<UserAccount> allUsers = userAccountRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<UserAccount> users = allUsers.stream()
            .filter(ua -> ua.getEmployeeId() != null)
            .collect(Collectors.toList());

        if (allUsers.size() > users.size()) {
            long filteredCount = allUsers.size() - users.size();
            log.warn("Filtered {} user account(s) with null employeeId from tenant: {}. " +
                    "This indicates data integrity issues that should be addressed.", filteredCount, tenantId);
        }

        return mapper.toResponseList(users);
    }

    @Override
    public List<UserAccountResponse> getUsersByRole(String tenantId, String role) {
        List<UserAccount> users = userAccountRepository.findByTenantIdAndRoleAndDeletedAtIsNull(tenantId, role);
        return mapper.toResponseList(users);
    }

    @Override
    public List<UserAccountResponse> getActiveUsers(String tenantId) {
        List<UserAccount> users = userAccountRepository.findByTenantIdAndIsActiveTrueAndDeletedAtIsNull(tenantId);
        return mapper.toResponseList(users);
    }

    @Override
    @Transactional
    public UserAccountResponse activateUser(Long id) {
        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userAccount.setIsActive(true);
        UserAccount updatedUser = userAccountRepository.save(userAccount);
        log.info("User account activated: {}", updatedUser.getUsername());

        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserAccountResponse deactivateUser(Long id) {
        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userAccount.setIsActive(false);
        UserAccount updatedUser = userAccountRepository.save(userAccount);
        log.info("User account deactivated: {}", updatedUser.getUsername());

        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserAccountResponse lockUser(Long id, String reason) {
        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userAccount.lockAccount();
        UserAccount updatedUser = userAccountRepository.save(userAccount);
        log.info("User account locked: {} - Reason: {}", updatedUser.getUsername(), reason);

        // Log activity
        activityLogService.logAccountLocked(id, reason, null);

        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserAccountResponse unlockUser(Long id) {
        UserAccount userAccount = userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userAccount.unlockAccount();
        UserAccount updatedUser = userAccountRepository.save(userAccount);
        log.info("User account unlocked: {}", updatedUser.getUsername());

        // Log activity
        activityLogService.logAccountUnlocked(id, null);

        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        UserAccount userAccount = userAccountRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordUtil.verifyPassword(request.getCurrentPassword(), userAccount.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password
        if (!passwordUtil.isPasswordValid(request.getNewPassword())) {
            throw new BadRequestException("New password does not meet complexity requirements");
        }

        // Update password
        userAccount.setPasswordHash(passwordUtil.hashPassword(request.getNewPassword()));
        userAccount.setPasswordChangedAt(OffsetDateTime.now());
        userAccount.setPasswordExpiresAt(OffsetDateTime.now().plusDays(passwordExpiryDays));
        userAccount.setMustChangePassword(false);

        userAccountRepository.save(userAccount);
        log.info("Password changed for user: {}", userAccount.getUsername());

        // Log activity
        activityLogService.logPasswordChanged(userAccount.getId(), null, null);

        return true;
    }

    @Override
    @Transactional
    public boolean forcePasswordChange(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userAccount.setMustChangePassword(true);
        userAccountRepository.save(userAccount);
        log.info("Force password change set for user: {}", userAccount.getUsername());

        return true;
    }

    @Override
    public boolean isUsernameAvailable(String tenantId, String username) {
        return !userAccountRepository.existsByTenantIdAndUsernameAndDeletedAtIsNull(tenantId, username);
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userAccount.incrementFailedLoginAttempts();

        // Lock account if threshold exceeded
        if (userAccount.getFailedLoginAttempts() >= failedLoginThreshold) {
            userAccount.lockAccount();
            log.warn("User account locked due to failed login attempts: {}", userAccount.getUsername());

            // Log activity
            activityLogService.logAccountLocked(
                userId,
                "Exceeded failed login threshold: " + failedLoginThreshold,
                null
            );
        }

        userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(Long userId) {
        UserAccount userAccount = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userAccount.resetFailedLoginAttempts();
        userAccountRepository.save(userAccount);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId, String ipAddress) {
        UserAccount userAccount = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userAccount.setLastLoginAt(OffsetDateTime.now());
        userAccount.setLastLoginIp(ipAddress);
        userAccountRepository.save(userAccount);
    }
}
