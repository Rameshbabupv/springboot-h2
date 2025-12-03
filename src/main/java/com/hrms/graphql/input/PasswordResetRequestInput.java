package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL Input type for password reset request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestInput {
    private String email;
    private String tenantId;
}
