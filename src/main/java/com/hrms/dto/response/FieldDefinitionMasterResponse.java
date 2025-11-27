package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinitionMasterResponse {

    private Long id;
    private String tenantId;
    private String fieldName;
    private String fieldLabel;
    private String fieldCode;
    private String fieldType;
    private String fieldCategory;
    private String dataType;
    private Map<String, Object> validationRules;
    @Builder.Default
    private List<Map<String, String>> dropdownOptions = new ArrayList<>();
    private Boolean isSystemField;
    private Boolean isCustomField;
    private Boolean isSearchable;
    private Boolean isRequiredByDefault;
    private String helpText;
    private String placeholderText;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
