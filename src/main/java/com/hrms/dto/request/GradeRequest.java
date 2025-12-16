package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Size(min = 2, max = 50, message = "Grade name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s&\\-/]+$", message = "Grade name can only contain letters, numbers, spaces, &, -, and /")
    private String name;

    @NotBlank(message = "Grade code is required")
    @Size(min = 2, max = 10, message = "Grade code must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "Grade code can only contain uppercase letters, numbers, -, and _")
    private String code;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;

    private String createdBy;

    private String updatedBy;
}
