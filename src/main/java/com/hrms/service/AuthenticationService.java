package com.hrms.service;

import com.hrms.dto.request.LoginRequest;
import com.hrms.dto.request.PasswordResetRequest;
import com.hrms.dto.request.PasswordResetRequestRequest;
import com.hrms.dto.response.AuthResponse;
import com.hrms.dto.response.PasswordResetResponse;

/**
 * Service interface for Authentication operations
 */
public interface AuthenticationService {

    /**
     * Authenticate user and create session
     */
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Logout user and invalidate session
     */
    boolean logout(String sessionToken);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Request password reset (send email with token)
     */
    PasswordResetResponse requestPasswordReset(PasswordResetRequestRequest request, String ipAddress);

    /**
     * Reset password using token
     */
    PasswordResetResponse resetPassword(PasswordResetRequest request);

    /**
     * Verify email using token
     */
    boolean verifyEmail(String token);

    /**
     * Send verification email
     */
    boolean sendVerificationEmail(Long userId);
}
