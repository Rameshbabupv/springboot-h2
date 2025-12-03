package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for City creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    private String cityName;

    @NotBlank(message = "State is required")
    private String state;

    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    private String pincode;

    @Builder.Default
    private Boolean isActive = true;
}
