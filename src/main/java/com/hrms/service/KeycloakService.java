package com.hrms.service;

import com.hrms.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 * Service for managing users and authentication in Keycloak.
 *
 * Responsibilities:
 * - Create new users in Keycloak
 * - Set user passwords
 * - Assign roles to users
 * - Update user attributes (tenant_id, company_ids, etc.)
 * - Authenticate users and retrieve tokens
 * - Refresh tokens
 * - Logout and invalidate sessions
 * - Sync roles between backend database and Keycloak
 */
@Slf4j
@Service
public class KeycloakService {

    @Value("${keycloak.server-url:http://localhost:8080}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:hrms}")
    private String realm;

    @Value("${keycloak.client-id:hrms-backend}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private Keycloak keycloakAdmin;

    public KeycloakService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Initialize Keycloak admin client for user management operations.
     * Called once on application startup via @PostConstruct.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            this.keycloakAdmin = Keycloak.getInstance(
                keycloakServerUrl,
                "master",
                "admin",
                "admin",
                "admin-cli"
            );
            log.info("Keycloak admin client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak admin client", e);
            throw new AuthenticationException(
                "KEYCLOAK_CONFIG_ERROR",
                "Failed to initialize Keycloak admin client: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Create a new user in Keycloak.
     *
     * @param username Username for the new user
     * @param email User's email address
     * @param tenantId Tenant identifier
     * @param firstName User's first name (optional)
     * @param lastName User's last name (optional)
     * @return Keycloak user ID (UUID)
     * @throws AuthenticationException if user already exists or creation fails
     */
    public String createUser(String username, String email, String tenantId,
                            String firstName, String lastName) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Check if user already exists
            List<UserRepresentation> existingUsers = usersResource.search(username, 0, 10);
            if (!existingUsers.isEmpty()) {
                throw new AuthenticationException(
                    "USER_EXISTS",
                    "Username already exists: " + username,
                    409
                );
            }

            // Create user representation
            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(username);
            userRepresentation.setEmail(email);
            userRepresentation.setFirstName(firstName != null ? firstName : "");
            userRepresentation.setLastName(lastName != null ? lastName : "");
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(false);

            // Set custom attributes
            Map<String, List<String>> attributes = new HashMap<>();
            attributes.put("tenant_id", Collections.singletonList(tenantId));
            attributes.put("company_ids", new ArrayList<>()); // Empty initially, will be set on login
            attributes.put("user_id", new ArrayList<>()); // Will be set from database
            userRepresentation.setAttributes(attributes);

            // Create user
            Response response = usersResource.create(userRepresentation);
            if (response.getStatus() != 201) {
                throw new AuthenticationException(
                    "KEYCLOAK_USER_CREATE_ERROR",
                    "Failed to create user in Keycloak: HTTP " + response.getStatus(),
                    500
                );
            }

            // Extract user ID from response
            String keycloakUserId = extractUserIdFromResponse(response);
            log.info("User created in Keycloak: {} (ID: {})", username, keycloakUserId);

            return keycloakUserId;

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: {}", username, e);
            throw new AuthenticationException(
                "KEYCLOAK_USER_CREATE_ERROR",
                "Failed to create user in Keycloak: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Set password for a user in Keycloak.
     *
     * @param keycloakUserId User's Keycloak ID (UUID)
     * @param password The password to set
     * @param temporary Whether this is a temporary password requiring reset
     * @throws AuthenticationException if password set fails
     */
    public void setUserPassword(String keycloakUserId, String password, boolean temporary) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            // Create credential representation for password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(temporary);

            userResource.resetPassword(credential);
            log.info("Password set for user: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("Error setting password for user: {}", keycloakUserId, e);
            throw new AuthenticationException(
                "KEYCLOAK_PASSWORD_SET_ERROR",
                "Failed to set password in Keycloak: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Assign a role to a user.
     *
     * @param keycloakUserId User's Keycloak ID (UUID)
     * @param roleName Role name to assign (e.g., "ADMIN", "MANAGER", "PORTAL")
     * @throws AuthenticationException if role assignment fails
     */
    public void assignRoleToUser(String keycloakUserId, String roleName) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            // Get the role
            RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();

            // Assign role to user
            userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
            log.info("Role assigned to user: {} -> {}", keycloakUserId, roleName);

        } catch (Exception e) {
            log.error("Error assigning role to user: {} role: {}", keycloakUserId, roleName, e);
            throw new AuthenticationException(
                "KEYCLOAK_ROLE_ASSIGN_ERROR",
                "Failed to assign role in Keycloak: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Update user attributes in Keycloak.
     *
     * @param keycloakUserId User's Keycloak ID (UUID)
     * @param attributes Map of attribute names to values
     * @throws AuthenticationException if update fails
     */
    public void updateUserAttributes(String keycloakUserId, Map<String, Object> attributes) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(keycloakUserId);

            UserRepresentation userRepresentation = userResource.toRepresentation();

            // Convert attributes
            Map<String, List<String>> attrs = new HashMap<>();
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                if (entry.getValue() instanceof List) {
                    attrs.put(entry.getKey(), (List<String>) entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    attrs.put(entry.getKey(), Collections.singletonList((String) entry.getValue()));
                } else if (entry.getValue() != null) {
                    attrs.put(entry.getKey(), Collections.singletonList(entry.getValue().toString()));
                }
            }

            userRepresentation.setAttributes(attrs);
            userResource.update(userRepresentation);
            log.info("User attributes updated: {}", keycloakUserId);

        } catch (Exception e) {
            log.error("Error updating user attributes: {}", keycloakUserId, e);
            throw new AuthenticationException(
                "KEYCLOAK_UPDATE_ATTR_ERROR",
                "Failed to update user attributes in Keycloak: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Get Keycloak user ID by username (for finding user after creation).
     *
     * @param username Username to search for
     * @return Keycloak user ID (UUID), or null if not found
     */
    public String getUserIdByUsername(String username) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(username, 0, 10);

            if (users.isEmpty()) {
                return null;
            }

            return users.get(0).getId();

        } catch (Exception e) {
            log.error("Error searching user by username: {}", username, e);
            return null;
        }
    }

    /**
     * Extract user ID from Keycloak response.
     * The user ID is returned in the Location header.
     *
     * @param response HTTP response from user creation
     * @return User ID (UUID)
     */
    private String extractUserIdFromResponse(Response response) {
        try {
            String location = response.getHeaderString("Location");
            if (location != null && !location.isEmpty()) {
                // Location format: http://keycloak/admin/realms/hrms/users/{id}
                return location.substring(location.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            log.error("Error extracting user ID from response", e);
        }
        throw new AuthenticationException(
            "KEYCLOAK_RESPONSE_ERROR",
            "Could not extract user ID from Keycloak response",
            500
        );
    }

    /**
     * Get Keycloak realm resource for direct admin operations.
     * Use with caution - for advanced operations only.
     *
     * @return Keycloak RealmResource
     */
    public RealmResource getRealmResource() {
        if (keycloakAdmin == null) {
            init();
        }
        return keycloakAdmin.realm(realm);
    }

    /**
     * Health check for Keycloak connectivity.
     *
     * @return true if Keycloak is reachable, false otherwise
     */
    public boolean isHealthy() {
        try {
            if (keycloakAdmin == null) {
                init();
            }
            keycloakAdmin.realm(realm).toRepresentation();
            return true;
        } catch (Exception e) {
            log.error("Keycloak health check failed", e);
            return false;
        }
    }


    /**
     * Get access token from Keycloak using username and password.
     * Uses OAuth2 Resource Owner Password Credentials grant.
     *
     * @param username Username
     * @param password Password
     * @return Map containing access_token, refresh_token, expires_in
     * @throws AuthenticationException if authentication fails
     */
    public Map<String, Object> getToken(String username, String password) {
        try {
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            // Build form data
            org.springframework.util.LinkedMultiValueMap<String, String> formData = 
                new org.springframework.util.LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("username", username);
            formData.add("password", password);

            // Set headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = 
                new org.springframework.http.HttpEntity<>(formData, headers);

            // Call Keycloak token endpoint
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, request, Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new AuthenticationException(
                    "TOKEN_ERROR",
                    "Failed to get token from Keycloak",
                    401
                );
            }

            log.info("Token obtained for user: {}", username);
            return response;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Keycloak token error: {}", e.getResponseBodyAsString());
            throw new AuthenticationException(
                "INVALID_CREDENTIALS",
                "Invalid username or password",
                401
            );
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting token from Keycloak", e);
            throw new AuthenticationException(
                "TOKEN_ERROR",
                "Failed to get token: " + e.getMessage(),
                500
            );
        }
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshToken Refresh token
     * @return Map containing new access_token, refresh_token, expires_in
     * @throws AuthenticationException if refresh fails
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            org.springframework.util.LinkedMultiValueMap<String, String> formData = 
                new org.springframework.util.LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("refresh_token", refreshToken);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = 
                new org.springframework.http.HttpEntity<>(formData, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, request, Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new AuthenticationException(
                    "TOKEN_REFRESH_ERROR",
                    "Failed to refresh token",
                    401
                );
            }

            log.info("Token refreshed successfully");
            return response;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Token refresh error: {}", e.getResponseBodyAsString());
            throw new AuthenticationException(
                "TOKEN_REFRESH_ERROR",
                "Refresh token expired or invalid",
                401
            );
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new AuthenticationException(
                "TOKEN_REFRESH_ERROR",
                "Failed to refresh token: " + e.getMessage(),
                500
            );
        }
    }
}
