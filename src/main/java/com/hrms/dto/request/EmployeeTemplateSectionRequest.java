package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateSectionRequest {

    @NotNull(message = "Template ID is required")
    private Long templateId;

    @NotBlank(message = "Section name is required")
    @Size(max = 200, message = "Section name must not exceed 200 characters")
    private String sectionName;

    @NotBlank(message = "Section code is required")
    @Size(max = 100, message = "Section code must not exceed 100 characters")
    private String sectionCode;

    private String sectionDescription;

    @NotNull(message = "Section order is required")
    private Integer sectionOrder;

    @Size(max = 50, message = "Section icon must not exceed 50 characters")
    private String sectionIcon;

    private Boolean isCollapsible = true;

    private Boolean isExpandedByDefault = true;

    private Map<String, Object> conditionalLogic;
}
