package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Division creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Division name is required")
    @Size(min = 2, max = 100, message = "Division name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Division code is required")
    @Size(min = 2, max = 20, message = "Division code must be between 2 and 20 characters")
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
