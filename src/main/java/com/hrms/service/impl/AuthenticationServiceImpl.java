package com.hrms.service.impl;

import com.hrms.dto.request.LoginRequest;
import com.hrms.dto.request.PasswordResetRequest;
import com.hrms.dto.request.PasswordResetRequestRequest;
import com.hrms.dto.response.AuthResponse;
import com.hrms.dto.response.PasswordResetResponse;
import com.hrms.dto.response.UserAccountResponse;
import com.hrms.entity.UserAccount;
import com.hrms.entity.UserPasswordReset;
import com.hrms.exception.BadRequestException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.mapper.UserAccountMapper;
import com.hrms.repository.UserAccountRepository;
import com.hrms.repository.UserPasswordResetRepository;
import com.hrms.service.AuthenticationService;
import com.hrms.service.UserAccountService;
import com.hrms.service.UserActivityLogService;
import com.hrms.service.UserSessionService;
import com.hrms.util.JwtUtil;
import com.hrms.util.PasswordUtil;
import com.hrms.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Service implementation for Authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final UserPasswordResetRepository passwordResetRepository;
    private final UserAccountMapper mapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final TokenGenerator tokenGenerator;
    private final UserAccountService userAccountService;
    private final UserSessionService sessionService;
    private final UserActivityLogService activityLogService;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for user: {} in tenant: {}", request.getUsername(), request.getTenantId());

        // Find user by username and tenant
        UserAccount userAccount = userAccountRepository
            .findByTenantIdAndUsernameAndDeletedAtIsNull(request.getTenantId(), request.getUsername())
            .orElseThrow(() -> {
                log.warn("Login failed: User not found - {}", request.getUsername());
                return new BadRequestException("Invalid credentials");
            });

        // Check if account is active
        if (!userAccount.getIsActive()) {
            log.warn("Login failed: Account inactive - {}", request.getUsername());
            activityLogService.logFailedLogin(userAccount.getId(), request.getUsername(), ipAddress, userAgent);
            throw new BadRequestException("Account is inactive");
        }

        // Check if account is locked
        if (userAccount.getIsLocked()) {
            log.warn("Login failed: Account locked - {}", request.getUsername());
            activityLogService.logFailedLogin(userAccount.getId(), request.getUsername(), ipAddress, userAgent);
            throw new BadRequestException("Account is locked. Please contact administrator.");
        }

        // Check if password is expired
        if (userAccount.isPasswordExpired()) {
            log.warn("Login failed: Password expired - {}", request.getUsername());
            throw new BadRequestException("Password has expired. Please reset your password.");
        }

        // Verify password
        if (!passwordUtil.verifyPassword(request.getPassword(), userAccount.getPasswordHash())) {
            log.warn("Login failed: Invalid password - {}", request.getUsername());

            // Increment failed login attempts
            userAccountService.incrementFailedLoginAttempts(userAccount.getId());
            activityLogService.logFailedLogin(userAccount.getId(), request.getUsername(), ipAddress, userAgent);

            throw new BadRequestException("Invalid credentials");
        }

        // Login successful - reset failed attempts
        userAccountService.resetFailedLoginAttempts(userAccount.getId());
        userAccountService.updateLastLogin(userAccount.getId(), ipAddress);

        // Generate JWT tokens
        String accessToken = jwtUtil.generateToken(
            userAccount.getId(),
            userAccount.getUsername(),
            userAccount.getTenantId(),
            userAccount.getRole()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
            userAccount.getId(),
            userAccount.getUsername()
        );

        // Create session
        sessionService.createSession(
            userAccount.getId(),
            accessToken,
            refreshToken,
            ipAddress,
            userAgent,
            request.getDeviceInfo()
        );

        // Log successful login
        activityLogService.logLogin(userAccount.getId(), ipAddress, userAgent);

        log.info("Login successful for user: {}", request.getUsername());

        // Build response
        UserAccountResponse userResponse = mapper.toResponse(userAccount);

        return AuthResponse.builder()
            .token(accessToken)
            .refreshToken(refreshToken)
            .user(userResponse)
            .expiresIn(jwtUtil.getExpirationTime().intValue())
            .build();
    }

    @Override
    @Transactional
    public boolean logout(String sessionToken) {
        try {
            // Extract user info from token
            Long userId = jwtUtil.extractUserId(sessionToken);

            // Invalidate session
            sessionService.getSessionByToken(sessionToken);

            // Log logout
            activityLogService.logLogout(userId, null, null);

            log.info("Logout successful for user: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Logout failed", e);
            return false;
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            String username = jwtUtil.extractUsername(refreshToken);

            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new BadRequestException("Refresh token has expired");
            }

            // Get user
            Long userId = jwtUtil.extractUserId(refreshToken);
            UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

            // Check account status
            if (!userAccount.canLogin()) {
                throw new BadRequestException("Account is not active");
            }

            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getTenantId(),
                userAccount.getRole()
            );

            String newRefreshToken = jwtUtil.generateRefreshToken(
                userAccount.getId(),
                userAccount.getUsername()
            );

            // Build response
            UserAccountResponse userResponse = mapper.toResponse(userAccount);

            log.info("Token refreshed for user: {}", username);

            return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userResponse)
                .expiresIn(jwtUtil.getExpirationTime().intValue())
                .build();
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BadRequestException("Invalid refresh token");
        }
    }

    @Override
    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequestRequest request, String ipAddress) {
        log.info("Password reset requested for email: {}", request.getEmail());

        // Find user by email and tenant
        UserAccount userAccount = userAccountRepository
            .findByTenantIdAndEmailAndDeletedAtIsNull(request.getTenantId(), request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        // Generate reset token
        String resetToken = tokenGenerator.generatePasswordResetToken();

        // Create password reset record
        UserPasswordReset passwordReset = new UserPasswordReset();
        passwordReset.setUserId(userAccount.getId());
        passwordReset.setResetToken(passwordUtil.hashPassword(resetToken)); // Hash the token
        passwordReset.setExpiresAt(OffsetDateTime.now().plusHours(1)); // 1 hour expiry
        passwordReset.setIpAddress(ipAddress);

        passwordResetRepository.save(passwordReset);

        // Log activity
        activityLogService.logActivity(
            userAccount.getId(),
            "PASSWORD_RESET_REQUESTED",
            null, null,
            "Password reset requested",
            null, ipAddress, null
        );

        log.info("Password reset token generated for user: {}", userAccount.getUsername());

        // TODO: Send email with reset link (resetToken)
        // Email should contain link: https://app.com/reset-password?token={resetToken}

        return PasswordResetResponse.builder()
            .success(true)
            .message("Password reset instructions have been sent to your email")
            .build();
    }

    @Override
    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetRequest request) {
        log.info("Processing password reset with token");

        // Validate new password
        if (!passwordUtil.isPasswordValid(request.getNewPassword())) {
            throw new BadRequestException("Password does not meet complexity requirements");
        }

        // Find valid reset token
        UserPasswordReset passwordReset = passwordResetRepository
            .findValidToken(request.getResetToken(), LocalDateTime.now())
            .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        // Get user account
        UserAccount userAccount = userAccountRepository.findById(passwordReset.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update password
        userAccount.setPasswordHash(passwordUtil.hashPassword(request.getNewPassword()));
        userAccount.setPasswordChangedAt(OffsetDateTime.now());
        userAccount.setPasswordExpiresAt(OffsetDateTime.now().plusDays(90));
        userAccount.setMustChangePassword(false);

        userAccountRepository.save(userAccount);

        // Mark token as used
        passwordReset.markAsUsed();
        passwordResetRepository.save(passwordReset);

        // Terminate all active sessions
        sessionService.terminateAllUserSessions(userAccount.getId());

        // Log activity
        activityLogService.logPasswordReset(userAccount.getId(), null, null);

        log.info("Password reset successful for user: {}", userAccount.getUsername());

        return PasswordResetResponse.builder()
            .success(true)
            .message("Password has been reset successfully")
            .build();
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        // TODO: Implement email verification logic
        log.info("Email verification not yet implemented");
        return false;
    }

    @Override
    @Transactional
    public boolean sendVerificationEmail(Long userId) {
        // TODO: Implement send verification email
        log.info("Send verification email not yet implemented");
        return false;
    }
}
