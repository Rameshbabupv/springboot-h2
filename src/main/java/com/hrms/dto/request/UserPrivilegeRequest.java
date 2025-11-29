package com.hrms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for UserPrivilege operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivilegeRequest {
    private String moduleCode;
    private Boolean canView;
    private Boolean canAdd;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canApprove;
    private Boolean canBackdate;
    private Integer backdateDays;
    private Map<String, Object> menuOverrides;
}
