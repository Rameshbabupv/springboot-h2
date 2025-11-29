package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for HrmsModule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmsModuleResponse {
    private Long id;
    private String tenantId;
    private String moduleCode;
    private String moduleName;
    private String moduleCategory;
    private String parentModuleCode;
    private Integer displayOrder;
    private Boolean isActive;
}
