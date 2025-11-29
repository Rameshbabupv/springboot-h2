package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for UserOrganizationalScope
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationalScopeResponse {
    private Long id;
    private String tenantId;
    private Long userId;
    private String scopeType;
    private Long scopeId;
    private LocalDateTime createdAt;
    private String createdBy;
}
