package com.hrms.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility class for generating secure random tokens
 * - Password reset tokens
 * - Email verification tokens
 * - Session tokens
 */
@Component
public class TokenGenerator {

    private final SecureRandom secureRandom;

    public TokenGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate secure random token using UUID
     *
     * @return Random UUID token
     */
    public String generateUUIDToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate secure random token using SecureRandom
     *
     * @param length Token length in bytes (will be Base64 encoded, so output will be longer)
     * @return Base64 encoded random token
     */
    public String generateSecureToken(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generate password reset token (32 bytes, Base64 encoded)
     *
     * @return Password reset token
     */
    public String generatePasswordResetToken() {
        return generateSecureToken(32);
    }

    /**
     * Generate email verification token (24 bytes, Base64 encoded)
     *
     * @return Email verification token
     */
    public String generateEmailVerificationToken() {
        return generateSecureToken(24);
    }

    /**
     * Generate numeric OTP (One-Time Password)
     *
     * @param length OTP length (default 6)
     * @return Numeric OTP
     */
    public String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Generate 6-digit OTP
     *
     * @return 6-digit OTP
     */
    public String generateOTP() {
        return generateOTP(6);
    }
}
