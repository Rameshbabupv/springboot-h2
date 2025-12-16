package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for authentication operations (signup/login).
 *
 * Contains:
 * - User information (id, username, email)
 * - Tenant and company context
 * - JWT tokens
 * - User roles
 *
 * Note: Both 'token' and 'accessToken' fields exist for backward compatibility.
 * New code should use 'accessToken'. Legacy code uses 'token'.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * Database user ID (primary key from user_accounts table).
     */
    private Long userId;

    /**
     * Username used for login.
     */
    private String username;

    /**
     * User's email address.
     */
    private String email;

    /**
     * Tenant ID (Nano ID format, 21 characters).
     * All data for this user is scoped to this tenant.
     */
    private String tenantId;

    /**
     * List of company IDs the user has access to.
     * User can query/operate on data from any of these companies.
     */
    private List<Long> companyIds;

    /**
     * Currently selected company ID.
     * Default operations happen in this company context.
     */
    private Long currentCompanyId;

    /**
     * List of role names assigned to the user.
     * e.g., ["app_admin"], ["manager", "portal"]
     */
    private List<String> roles;

    /**
     * JWT access token (legacy field name).
     * @deprecated Use accessToken instead
     */
    private String token;

    /**
     * JWT access token.
     * Include in Authorization header as "Bearer {accessToken}".
     */
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens.
     */
    private String refreshToken;

    /**
     * Access token expiry time in seconds.
     */
    private Integer expiresIn;

    /**
     * Optional message (e.g., "Signup successful", "Login successful").
     */
    private String message;

    /**
     * Legacy: Full user object for backward compatibility.
     */
    private UserAccountResponse user;
}
