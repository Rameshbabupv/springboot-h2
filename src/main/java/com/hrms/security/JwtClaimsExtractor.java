package com.hrms.security;

import com.hrms.exception.AuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility component for extracting claims from JWT tokens.
 *
 * Used by GraphQL resolvers to get tenant_id, company_ids, user_id
 * from the authenticated user's JWT token.
 *
 * Claims expected in JWT:
 * - tenant_id: String (Nano ID format)
 * - company_ids: List<Long> (user's accessible companies)
 * - user_id: Long (database user_accounts.id)
 * - roles: List<String> (user's roles)
 */
@Slf4j
@Component
public class JwtClaimsExtractor {

    /**
     * Get the current JWT from SecurityContext.
     *
     * @return Jwt token or null if not authenticated
     */
    public Jwt getCurrentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt) {
            return (Jwt) auth.getPrincipal();
        }
        return null;
    }

    /**
     * Get tenant ID from current JWT.
     *
     * @return Tenant ID string
     * @throws AuthorizationException if not authenticated or claim missing
     */
    public String getTenantId() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            throw new AuthorizationException("NOT_AUTHENTICATED", "No valid JWT token", 401);
        }

        String tenantId = jwt.getClaim("tenant_id");
        if (tenantId == null || tenantId.isEmpty()) {
            throw new AuthorizationException("MISSING_TENANT", "JWT missing tenant_id claim", 403);
        }

        return tenantId;
    }

    /**
     * Get tenant ID from current JWT, or return provided fallback.
     * Useful for backward compatibility during migration.
     *
     * @param fallbackTenantId Fallback tenant ID if JWT not present
     * @return Tenant ID from JWT or fallback
     */
    public String getTenantIdOrFallback(String fallbackTenantId) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            log.debug("No JWT found, using fallback tenantId: {}", fallbackTenantId);
            return fallbackTenantId;
        }

        String tenantId = jwt.getClaim("tenant_id");
        if (tenantId == null || tenantId.isEmpty()) {
            log.debug("JWT missing tenant_id, using fallback: {}", fallbackTenantId);
            return fallbackTenantId;
        }

        return tenantId;
    }

    /**
     * Get list of company IDs the user has access to.
     *
     * @return List of company IDs
     * @throws AuthorizationException if not authenticated
     */
    @SuppressWarnings("unchecked")
    public List<Long> getCompanyIds() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            throw new AuthorizationException("NOT_AUTHENTICATED", "No valid JWT token", 401);
        }

        Object companyIdsClaim = jwt.getClaim("company_ids");
        if (companyIdsClaim == null) {
            return Collections.emptyList();
        }

        if (companyIdsClaim instanceof List) {
            List<?> list = (List<?>) companyIdsClaim;
            return list.stream()
                .map(item -> {
                    if (item instanceof Number) {
                        return ((Number) item).longValue();
                    }
                    return Long.valueOf(item.toString());
                })
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Get user ID from JWT.
     *
     * @return User ID (database primary key)
     * @throws AuthorizationException if not authenticated or claim missing
     */
    public Long getUserId() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            throw new AuthorizationException("NOT_AUTHENTICATED", "No valid JWT token", 401);
        }

        Object userIdClaim = jwt.getClaim("user_id");
        if (userIdClaim == null) {
            throw new AuthorizationException("MISSING_USER_ID", "JWT missing user_id claim", 403);
        }

        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }
        return Long.valueOf(userIdClaim.toString());
    }

    /**
     * Get user ID from JWT, or return null if not present.
     *
     * @return User ID or null
     */
    public Long getUserIdOrNull() {
        try {
            return getUserId();
        } catch (AuthorizationException e) {
            return null;
        }
    }

    /**
     * Get roles from JWT.
     *
     * @return List of role names
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return Collections.emptyList();
        }

        Object rolesClaim = jwt.getClaim("roles");
        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        }

        return Collections.emptyList();
    }

    /**
     * Validate that user has access to the requested company IDs.
     *
     * @param requestedCompanyIds Company IDs requested in query
     * @return Validated company IDs (subset of user's accessible companies)
     * @throws AuthorizationException if any requested company is not accessible
     */
    public List<Long> validateCompanyAccess(List<Long> requestedCompanyIds) {
        List<Long> allowedCompanyIds = getCompanyIds();

        if (requestedCompanyIds == null || requestedCompanyIds.isEmpty()) {
            // No filter requested, return all allowed companies
            return allowedCompanyIds;
        }

        // Validate all requested companies are in allowed list
        for (Long companyId : requestedCompanyIds) {
            if (!allowedCompanyIds.contains(companyId)) {
                throw new AuthorizationException(
                    "COMPANY_ACCESS_DENIED",
                    "Access denied to company: " + companyId,
                    403
                );
            }
        }

        return requestedCompanyIds;
    }

    /**
     * Check if user is authenticated.
     *
     * @return true if valid JWT present
     */
    public boolean isAuthenticated() {
        return getCurrentJwt() != null;
    }

    /**
     * Check if user has a specific role.
     *
     * @param role Role name to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }
}
