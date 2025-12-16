package com.hrms.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @NotNull(message = "Country ID is required")
    private Long countryId;

    @NotNull(message = "State ID is required")
    private Long stateId;

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s&\\-/]+$", message = "City name can only contain letters, numbers, spaces, &, -, and /")
    private String name;

    @NotBlank(message = "City code is required")
    @Size(min = 2, max = 10, message = "City code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "City code can only contain uppercase letters, numbers, -, and _")
    private String code;

    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    private String pincode;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private String createdBy;

    private String updatedBy;
}
