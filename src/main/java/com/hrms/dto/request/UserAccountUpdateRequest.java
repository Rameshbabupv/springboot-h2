package com.hrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountUpdateRequest {

    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain alphanumeric characters, dots, underscores, and hyphens")
    private String username;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    private String role;

    private Boolean isActive;

    private Boolean isLocked;

    private Boolean mustChangePassword;

    private Boolean inheritFromDesignation;
}
