package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Country creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Country name is required")
    @Size(min = 2, max = 100, message = "Country name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s&\\-/]+$", message = "Country name can only contain letters, spaces, &, -, and /")
    private String name;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 10, message = "Country code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "Country code can only contain uppercase letters, numbers, -, and _")
    private String code;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a valid 3-letter ISO code (e.g., USD, INR)")
    private String currencyCode;

    @Size(max = 10, message = "Phone code must not exceed 10 characters")
    @Pattern(regexp = "^\\+?[0-9]+$", message = "Phone code can only contain numbers and optional + prefix")
    private String phoneCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private String createdBy;

    private String updatedBy;
}
