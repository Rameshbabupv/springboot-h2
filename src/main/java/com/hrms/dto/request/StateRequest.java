package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for State creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Country ID is required")
    private Long countryId;

    @NotBlank(message = "State name is required")
    @Size(min = 2, max = 50, message = "State name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s&\\-/]+$", message = "State name can only contain letters, spaces, &, -, and /")
    private String name;

    @NotBlank(message = "State code is required")
    @Size(min = 2, max = 10, message = "State code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "State code can only contain uppercase letters, numbers, -, and _")
    private String code;

    @Size(max = 10, message = "State code must not exceed 10 characters")
    private String stateCode;

    @Builder.Default
    private Boolean isUnionTerritory = false;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private String createdBy;

    private String updatedBy;
}
