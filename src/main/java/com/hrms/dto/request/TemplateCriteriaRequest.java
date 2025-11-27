package com.hrms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCriteriaRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    private String category;

    private String group;

    private String grade;

    private UUID companyId;

    private UUID locationId;
}
