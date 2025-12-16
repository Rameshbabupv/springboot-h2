package com.hrms.service;

import com.hrms.dto.request.LoginRequest;
import com.hrms.dto.request.SignupRequest;
import com.hrms.dto.response.AuthResponse;
import com.hrms.dto.response.TokenResponse;
import com.hrms.entity.Company;
import com.hrms.entity.UserAccount;
import com.hrms.entity.UserCompanyAccess;
import com.hrms.exception.AuthenticationException;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.UserAccountRepository;
import com.hrms.repository.UserCompanyAccessRepository;
import com.hrms.util.PasswordValidator;
import com.hrms.util.TenantIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Authentication Service handling signup and login flows.
 *
 * Responsibilities:
 * - User signup (create tenant, company, first user)
 * - User login (validate credentials, generate JWT)
 * - Token refresh
 * - Logout
 * - Coordinate between database and Keycloak
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KeycloakService keycloakService;
    private final UserAccountRepository userAccountRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyAccessRepository userCompanyAccessRepository;

    /**
     * User signup - creates new tenant, company, and first user.
     *
     * First user gets app_admin role to manage other users for the company.
     *
     * @param request Signup request with userId, companyName, email, phone, password
     * @return AuthResponse with JWT token and user details
     * @throws AuthenticationException if signup fails
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        try {
            // Step 1: Validate input
            log.info("Processing signup for user: {}", request.getUserId());

            // Validate password
            PasswordValidator.ValidationResult passwordValidation =
                PasswordValidator.validate(request.getPassword());
            if (!passwordValidation.isValid()) {
                throw new AuthenticationException(
                    "INVALID_PASSWORD",
                    "Password does not meet requirements: " + passwordValidation.getErrorMessage(),
                    400
                );
            }

            // Check email uniqueness in database
            if (userAccountRepository.existsByEmail(request.getEmail())) {
                throw new AuthenticationException(
                    "EMAIL_EXISTS",
                    "Email already registered: " + request.getEmail(),
                    409
                );
            }

            // Step 2: Generate tenant ID (Nano ID)
            String tenantId = TenantIdGenerator.generate();
            log.info("Generated tenant ID: {}", tenantId);

            // Step 3: Create company in database
            Company company = new Company();
            company.setTenantId(tenantId);
            company.setName(request.getCompanyName());
            // Generate unique company code from name (first 10 chars of name + random suffix)
            String companyCode = generateCompanyCode(request.getCompanyName());
            company.setCode(companyCode);
            company.setIsActive(true);
            company.setCreatedAt(OffsetDateTime.now());
            company = companyRepository.save(company);
            log.info("Company created: {} (ID: {})", request.getCompanyName(), company.getId());

            // Step 4: Create user in Keycloak
            String keycloakUserId = keycloakService.createUser(
                request.getUserId(),
                request.getEmail(),
                tenantId,
                request.getFirstName(),
                request.getLastName()
            );

            // Step 5: Set password in Keycloak
            keycloakService.setUserPassword(keycloakUserId, request.getPassword(), false);

            // Step 6: Assign app_admin role (first user gets admin)
            keycloakService.assignRoleToUser(keycloakUserId, "app_admin");
            log.info("Role app_admin assigned to user: {}", keycloakUserId);

            // Step 7: Create UserAccount in database
            UserAccount user = new UserAccount();
            user.setKeycloakUserId(keycloakUserId);
            user.setUsername(request.getUserId());
            user.setEmail(request.getEmail());
            user.setTenantId(tenantId);
            user.setCompanyId(company.getId()); // Link to company
            user.setRole("app_admin"); // First user is always app_admin
            user.setPasswordHash("KEYCLOAK_MANAGED"); // Password managed by Keycloak
            user.setIsActive(true);
            user.setCreatedAt(OffsetDateTime.now());
            user = userAccountRepository.save(user);
            log.info("UserAccount created in database: {} (ID: {})", request.getUserId(), user.getId());

            // Step 8: Create user-company association
            UserCompanyAccess access = new UserCompanyAccess();
            access.setUserId(user.getId());
            access.setCompanyId(company.getId());
            access.setTenantId(tenantId);
            access.setIsActive(true);
            access.setGrantedBy(user.getId()); // Self-granted
            access.setGrantedAt(OffsetDateTime.now());
            userCompanyAccessRepository.save(access);
            log.info("User-company association created");

            // Step 9: Update Keycloak attributes with database IDs
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("user_id", user.getId().toString());
            attributes.put("company_ids", List.of(company.getId().toString()));
            keycloakService.updateUserAttributes(keycloakUserId, attributes);

            // Step 10: Generate auth response
            AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(tenantId)
                .companyIds(List.of(company.getId()))
                .currentCompanyId(company.getId())
                .roles(List.of("app_admin"))
                .accessToken("") // Will be set by frontend login
                .refreshToken("")
                .expiresIn(3600)
                .message("Signup successful. User created with app_admin role.")
                .build();

            log.info("Signup completed successfully for user: {}", request.getUserId());
            return response;

        } catch (AuthenticationException e) {
            log.error("Signup failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during signup", e);
            throw new AuthenticationException(
                "SIGNUP_ERROR",
                "Signup failed: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * User login - validate credentials and return JWT token.
     *
     * @param request Login request with username and password
     * @return AuthResponse with JWT token and user details
     * @throws AuthenticationException if credentials invalid or login fails
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            log.info("Processing login for user: {}", request.getUsername());

            // Step 1: Validate credentials with Keycloak
            // Note: This would be done via Keycloak token endpoint in production
            // For now, find user in database

            UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException(
                    "INVALID_CREDENTIALS",
                    "Invalid username or password",
                    401
                ));

            // Step 2: Check user status
            if (!user.getIsActive()) {
                throw new AuthenticationException(
                    "ACCOUNT_INACTIVE",
                    "Account is inactive. Please contact administrator.",
                    403
                );
            }

            // Step 3: Get user's company access
            List<UserCompanyAccess> companyAccess = userCompanyAccessRepository
                .findByUserIdAndIsActiveTrue(user.getId());

            if (companyAccess.isEmpty()) {
                throw new AuthenticationException(
                    "COMPANY_ACCESS_DENIED",
                    "User has no company access configured",
                    403
                );
            }

            List<Long> companyIds = companyAccess.stream()
                .map(UserCompanyAccess::getCompanyId)
                .toList();

            Long currentCompanyId = user.getCompanyId() != null ? user.getCompanyId() : companyIds.get(0);

            // Step 4: Get user roles from database (source of truth)
            // For now, query from user entity - will be enhanced with role table
            List<String> roles = getDefaultRolesForUser(user);

            // Step 5: Update Keycloak attributes with latest data
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("company_ids", companyIds.stream()
                .map(Object::toString)
                .toList());
            attributes.put("user_id", user.getId().toString());
            keycloakService.updateUserAttributes(user.getKeycloakUserId(), attributes);

            // Step 6: Build auth response
            // Note: Access token and refresh token would be generated from Keycloak
            // In a real implementation, this would return the actual JWT from Keycloak
            AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .companyIds(companyIds)
                .currentCompanyId(currentCompanyId)
                .roles(roles)
                .accessToken("") // Will be generated by frontend from Keycloak
                .refreshToken("")
                .expiresIn(3600)
                .message("Login successful")
                .build();

            log.info("Login completed successfully for user: {}", request.getUsername());
            return response;

        } catch (AuthenticationException e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            throw new AuthenticationException(
                "LOGIN_ERROR",
                "Login failed: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Logout user - invalidate tokens.
     *
     * @param keycloakUserId Keycloak user ID to logout
     */
    public void logout(String keycloakUserId) {
        try {
            log.info("Logging out user: {}", keycloakUserId);
            // Keycloak logout would be handled via token endpoint
            // Mark session as invalid if using stateful sessions
            log.info("User logged out successfully: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new AuthenticationException(
                "LOGOUT_ERROR",
                "Logout failed: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshToken Refresh token from client
     * @return TokenResponse with new access token
     * @throws AuthenticationException if refresh fails
     */
    public TokenResponse refreshToken(String refreshToken) {
        try {
            log.info("Refreshing token");
            // Token refresh would be handled via Keycloak token endpoint
            // This is typically a direct call to Keycloak from frontend
            throw new AuthenticationException(
                "TOKEN_REFRESH_ERROR",
                "Token refresh not yet implemented",
                501
            );
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new AuthenticationException(
                "TOKEN_REFRESH_ERROR",
                "Token refresh failed: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Get default roles for a user based on database configuration.
     * This is temporary - will be replaced with a proper role management table.
     *
     * @param user UserAccount entity
     * @return List of role names
     */
    private List<String> getDefaultRolesForUser(UserAccount user) {
        // Placeholder: Return default role for now
        // In production, query from user_roles table
        return List.of("portal");
    }

    /**
     * Generate a unique company code from company name.
     * Format: First 10 chars uppercase + 4 random alphanumeric chars
     * Example: "Test Company Inc" -> "TESTCOMPAN1A2B"
     *
     * @param companyName Company name
     * @return Unique company code (max 20 chars)
     */
    private String generateCompanyCode(String companyName) {
        // Remove spaces and special chars, uppercase, take first 10 chars
        String prefix = companyName.toUpperCase()
            .replaceAll("[^A-Z0-9]", "")
            .substring(0, Math.min(10, companyName.replaceAll("[^A-Za-z0-9]", "").length()));

        // Add random suffix for uniqueness
        String suffix = TenantIdGenerator.generate().substring(0, 4).toUpperCase();

        return prefix + suffix;
    }
}
