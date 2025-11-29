package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for UserPrivilege
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivilegeResponse {
    private Long id;
    private String tenantId;
    private Long userId;
    private String moduleCode;
    private Boolean canView;
    private Boolean canAdd;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canApprove;
    private Boolean canBackdate;
    private Integer backdateDays;
    private Map<String, Object> menuOverrides;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
