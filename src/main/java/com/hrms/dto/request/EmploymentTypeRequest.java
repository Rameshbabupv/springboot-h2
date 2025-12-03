package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for EmploymentType creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentTypeRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Employment type name is required")
    @Size(min = 2, max = 100, message = "Employment type name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Employment type code is required")
    @Size(min = 2, max = 20, message = "Employment type code must be between 2 and 20 characters")
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
