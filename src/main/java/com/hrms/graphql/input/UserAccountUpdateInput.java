package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL Input type for updating user account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountUpdateInput {
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private Boolean isLocked;
    private Boolean mustChangePassword;
}
