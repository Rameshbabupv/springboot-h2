package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinitionMasterRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Field name is required")
    @Size(max = 100, message = "Field name must not exceed 100 characters")
    private String fieldName;

    @NotBlank(message = "Field label is required")
    @Size(max = 200, message = "Field label must not exceed 200 characters")
    private String fieldLabel;

    @Size(max = 100, message = "Field code must not exceed 100 characters")
    private String fieldCode;

    @NotBlank(message = "Field type is required")
    @Pattern(regexp = "^(text|number|date|datetime|dropdown|file|checkbox|radio|textarea|email|phone|url|multiselect)$",
            message = "Invalid field type")
    private String fieldType;

    @Size(max = 100, message = "Field category must not exceed 100 characters")
    private String fieldCategory;

    @NotBlank(message = "Data type is required")
    @Pattern(regexp = "^(string|integer|date|boolean|json)$",
            message = "Invalid data type")
    private String dataType;

    private Map<String, Object> validationRules;

    private List<Map<String, String>> dropdownOptions = new ArrayList<>();

    private Boolean isSystemField = false;

    private Boolean isCustomField = false;

    private Boolean isSearchable = true;

    private Boolean isRequiredByDefault = false;

    private String helpText;

    @Size(max = 200, message = "Placeholder text must not exceed 200 characters")
    private String placeholderText;

    @Pattern(regexp = "^(active|inactive|draft)$", message = "Status must be active, inactive, or draft")
    private String status = "active";

    private Long createdBy;

    private Long updatedBy;
}
