package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL Input type for password reset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetInput {
    private String resetToken;
    private String newPassword;
}
