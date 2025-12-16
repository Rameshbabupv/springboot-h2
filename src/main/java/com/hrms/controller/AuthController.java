package com.hrms.controller;

import com.hrms.dto.request.LoginRequest;
import com.hrms.dto.request.SignupRequest;
import com.hrms.dto.response.AuthResponse;
import com.hrms.dto.response.ErrorResponse;
import com.hrms.dto.response.TokenResponse;
import com.hrms.exception.AuthenticationException;
import com.hrms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for authentication operations.
 *
 * Endpoints:
 * - POST /api/auth/signup - Create new tenant, company, and first user
 * - POST /api/auth/login - Authenticate user and get JWT
 * - POST /api/auth/refresh - Refresh access token
 * - POST /api/auth/logout - Invalidate session
 *
 * All endpoints return standardized error responses with error codes.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * User signup - creates new tenant, company, and first user.
     *
     * First user gets app_admin role automatically.
     * Creates:
     * - New tenant (Nano ID generated)
     * - New company record
     * - First user account with app_admin role
     *
     * @param request Signup request with userId, companyName, email, phone, password
     * @return AuthResponse with user details and tokens
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("REST: POST /api/auth/signup - userId: {}, email: {}",
                 request.getUserId(), request.getEmail());

        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AuthenticationException e) {
            log.error("Signup failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * User login - authenticate and get JWT tokens.
     *
     * @param request Login request with username and password
     * @return AuthResponse with JWT tokens and user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST: POST /api/auth/login - username: {}", request.getUsername());

        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshToken Refresh token from previous login
     * @return TokenResponse with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshRequest request) {
        log.info("REST: POST /api/auth/refresh");

        try {
            TokenResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Logout user - invalidate Keycloak session.
     *
     * @param jwt Current JWT from Authorization header
     * @return No content on success
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        log.info("REST: POST /api/auth/logout");

        if (jwt != null) {
            String keycloakUserId = jwt.getSubject();
            authService.logout(keycloakUserId);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Get current user info from JWT.
     * Useful for frontend to verify authentication state.
     *
     * @param jwt Current JWT from Authorization header
     * @return User info extracted from JWT
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.info("REST: GET /api/auth/me");

        if (jwt == null) {
            throw new AuthenticationException("NOT_AUTHENTICATED", "No valid JWT token", 401);
        }

        // Extract claims from JWT
        Long userId = jwt.getClaim("user_id") != null
            ? Long.valueOf(jwt.getClaim("user_id").toString())
            : null;
        String tenantId = jwt.getClaim("tenant_id");
        List<Long> companyIds = jwt.getClaim("company_ids");
        List<String> roles = jwt.getClaim("roles");

        AuthResponse response = AuthResponse.builder()
            .userId(userId)
            .username(jwt.getClaim("preferred_username"))
            .email(jwt.getClaim("email"))
            .tenantId(tenantId)
            .companyIds(companyIds)
            .roles(roles)
            .message("Authenticated")
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for AuthenticationException.
     * Returns standardized error response with error code.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        ErrorResponse response = ErrorResponse.builder()
            .errors(List.of(ErrorResponse.ErrorDetail.builder()
                .code(e.getErrorCode())
                .message(e.getMessage())
                .status(e.getHttpStatus())
                .build()))
            .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    /**
     * Simple DTO for refresh token request.
     */
    @lombok.Data
    public static class RefreshRequest {
        private String refreshToken;
    }
}
