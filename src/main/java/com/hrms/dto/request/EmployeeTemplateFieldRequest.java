package com.hrms.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateFieldRequest {

    @NotNull(message = "Template ID is required")
    private Long templateId;

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Field ID is required")
    private Long fieldId;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    @Pattern(regexp = "^(full|half|third|quarter)$", message = "Display width must be full, half, third, or quarter")
    private String displayWidth = "full";

    private Boolean isRequired = false;

    private Boolean isReadonly = false;

    private Boolean isVisible = true;

    private Boolean isEditable = true;

    private String labelOverride;

    private String helpTextOverride;

    private Map<String, Object> validationOverride;

    private String defaultValue;

    private Map<String, Object> conditionalLogic;
}
