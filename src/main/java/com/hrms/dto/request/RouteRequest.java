package com.hrms.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Route creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Route name is required")
    @Size(min = 2, max = 50, message = "Route name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s&\\-/]+$",
             message = "Route name can only contain letters, numbers, spaces, &, -, and /")
    private String name;

    @NotBlank(message = "Route code is required")
    @Size(min = 2, max = 10, message = "Route code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$",
             message = "Route code can only contain uppercase letters, numbers, -, and _")
    private String code;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private String createdBy;

    private String updatedBy;
}
