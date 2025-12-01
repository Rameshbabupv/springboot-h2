package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO for user account information
 * Excludes sensitive data like password hash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountResponse {

    private Long id;
    private String tenantId;
    private Long employeeId;
    private EmployeeResponse employee;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private Boolean isLocked;
    private Boolean isEmailVerified;
    private Integer failedLoginAttempts;
    private OffsetDateTime lastLoginAt;
    private String lastLoginIp;
    private OffsetDateTime passwordChangedAt;
    private OffsetDateTime passwordExpiresAt;
    private Boolean mustChangePassword;
    private Boolean inheritFromDesignation;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private OffsetDateTime updatedAt;
}
