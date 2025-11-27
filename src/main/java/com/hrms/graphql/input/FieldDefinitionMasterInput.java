package com.hrms.graphql.input;

import lombok.Data;

@Data
public class FieldDefinitionMasterInput {
    private String tenantId;
    private String fieldName;
    private String fieldLabel;
    private String fieldCode;
    private String fieldType;
    private String fieldCategory;
    private String dataType;
    private String validationRules;
    private String dropdownOptions;
    private Boolean isSystemField = false;
    private Boolean isCustomField = false;
    private Boolean isSearchable = true;
    private Boolean isRequiredByDefault = false;
    private String helpText;
    private String placeholderText;
    private String status = "active";
    private String createdBy;
    private String updatedBy;
}
