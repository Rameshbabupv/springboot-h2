package com.hrms.graphql.input;

import lombok.Data;

@Data
public class EmployeeTemplateFieldInput {
    private String templateId;
    private String sectionId;
    private String fieldId;
    private Integer displayOrder;
    private String displayWidth = "full";
    private Boolean isRequired = false;
    private Boolean isReadonly = false;
    private Boolean isVisible = true;
    private Boolean isEditable = true;
    private String labelOverride;
    private String helpTextOverride;
    private String validationOverride;
    private String defaultValue;
    private String conditionalLogic;
}
