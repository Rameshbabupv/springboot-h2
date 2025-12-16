package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for JobFunction creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobFunctionRequest {

    @NotBlank(message = "Tenant ID is required")
    @Size(max = 50, message = "Tenant ID must not exceed 50 characters")
    private String tenantId;

    @NotBlank(message = "Job function name is required")
    @Size(min = 2, max = 50, message = "Job function name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s&\\-/]+$", message = "Job function name can only contain letters, numbers, spaces, &, -, and /")
    private String name;

    @NotBlank(message = "Job function code is required")
    @Size(min = 2, max = 10, message = "Job function code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "Job function code can only contain uppercase letters, numbers, -, and _")
    private String code;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @NotBlank(message = "Function group is required")
    @Size(max = 50, message = "Function group must not exceed 50 characters")
    private String functionGroup;

    @Builder.Default
    private Boolean isActive = true;

    private String createdBy;

    private String updatedBy;
}
