package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL Input type for creating user account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountInput {
    private String tenantId;
    private Long employeeId;
    private String username;
    private String email;
    private String password;
    private String role;
    private Boolean isActive;
    private Boolean mustChangePassword;
    private Boolean inheritFromDesignation;
}
