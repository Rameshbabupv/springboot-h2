package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for token-related operations.
 *
 * Used by:
 * - Token refresh endpoint
 * - Initial token generation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    /**
     * JWT access token for API authentication.
     * Include in Authorization header as "Bearer {accessToken}".
     */
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens.
     * Used when access token expires.
     */
    private String refreshToken;

    /**
     * Access token expiry time in seconds.
     * Default: 3600 (1 hour)
     */
    private Integer expiresIn;

    /**
     * Token type (always "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Refresh token expiry time in seconds.
     * Default: 604800 (7 days)
     */
    private Integer refreshExpiresIn;
}
