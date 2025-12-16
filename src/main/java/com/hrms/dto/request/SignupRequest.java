package com.hrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user signup.
 *
 * First user signup creates:
 * - New tenant (Nano ID generated)
 * - New company
 * - First user account with app_admin role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    /**
     * Username for login (e.g., "john.doe").
     * User-provided, not auto-generated.
     */
    @NotBlank(message = "User ID is required")
    @Size(min = 3, max = 50, message = "User ID must be between 3 and 50 characters")
    private String userId;

    /**
     * Company name for the new tenant.
     * Creates the first company record.
     */
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;

    /**
     * User's email address.
     * Must be globally unique across all tenants.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Phone number (optional).
     * Can be null or empty.
     */
    private String phone;

    /**
     * Password for the account.
     * Must meet password policy: 8+ chars, uppercase, lowercase, digit, special char.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * User's first name (optional).
     * Will be stored in Keycloak.
     */
    private String firstName;

    /**
     * User's last name (optional).
     * Will be stored in Keycloak.
     */
    private String lastName;
}
