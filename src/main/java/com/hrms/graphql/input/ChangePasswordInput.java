package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GraphQL Input type for changing password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordInput {
    private Long userId;
    private String currentPassword;
    private String newPassword;
}
