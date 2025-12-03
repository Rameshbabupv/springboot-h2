package com.hrms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for OrganizationalScope operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationalScopeRequest {
    private String scopeType;
    private Long scopeId;
}
