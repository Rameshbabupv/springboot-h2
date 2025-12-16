package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for user login.
 *
 * For Keycloak-based auth (new system):
 * - tenantId is NOT required in login request
 * - Backend finds user by username globally
 * - JWT will contain tenant_id after authentication
 *
 * For legacy auth:
 * - tenantId can still be provided for backward compatibility
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * Tenant ID (optional for Keycloak auth, required for legacy auth).
     * If not provided, user is looked up globally by username.
     */
    private String tenantId;

    /**
     * Username for authentication.
     * Must match existing user_accounts.username.
     */
    @NotBlank(message = "Username is required")
    private String username;

    /**
     * Password for authentication.
     * Validated against Keycloak or password_hash depending on auth method.
     */
    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Optional device info for audit/security purposes.
     * Can include: deviceType, browser, os, ip, etc.
     */
    private Map<String, Object> deviceInfo;
}
