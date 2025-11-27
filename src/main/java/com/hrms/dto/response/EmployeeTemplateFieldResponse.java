package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateFieldResponse {

    private Long id;
    private Long templateId;
    private Long sectionId;
    private Long fieldId;
    private FieldDefinitionMasterResponse fieldDefinition;
    private Integer displayOrder;
    private String displayWidth;
    private Boolean isRequired;
    private Boolean isReadonly;
    private Boolean isVisible;
    private Boolean isEditable;
    private String labelOverride;
    private String helpTextOverride;
    private Map<String, Object> validationOverride;
    private String defaultValue;
    private Map<String, Object> conditionalLogic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
