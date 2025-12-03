package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for Grade creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Grade name is required")
    @Size(min = 2, max = 100, message = "Grade name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Grade code is required")
    @Size(min = 1, max = 20, message = "Grade code must be between 1 and 20 characters")
    private String code;

    @PositiveOrZero(message = "Level must be zero or positive")
    private Integer level;

    @PositiveOrZero(message = "Minimum salary must be zero or positive")
    private BigDecimal minSalary;

    @PositiveOrZero(message = "Maximum salary must be zero or positive")
    private BigDecimal maxSalary;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
