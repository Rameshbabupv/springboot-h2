package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * GraphQL Input type for login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginInput {
    private String tenantId;
    private String username;
    private String password;
    private Map<String, Object> deviceInfo;
}
