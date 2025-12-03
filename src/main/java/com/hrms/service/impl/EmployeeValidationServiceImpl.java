package com.hrms.service.impl;

import com.hrms.entity.EmployeeTemplateField;
import com.hrms.entity.FieldDefinitionMaster;
import com.hrms.repository.EmployeeTemplateFieldRepository;
import com.hrms.repository.FieldDefinitionMasterRepository;
import com.hrms.service.EmployeeValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeValidationServiceImpl implements EmployeeValidationService {

    private final FieldDefinitionMasterRepository fieldDefinitionRepository;
    private final EmployeeTemplateFieldRepository templateFieldRepository;

    @Override
    public ValidationResult validateEmployeeData(String tenantId, Long templateId, Map<String, Object> employeeData) {
        log.debug("Validating employee data for tenant: {} and template: {}", tenantId, templateId);

        List<FieldValidationResult> fieldErrors = new ArrayList<>();

        // Get all template fields with their definitions
        List<EmployeeTemplateField> templateFields = templateFieldRepository.findByTemplateId(templateId);

        for (EmployeeTemplateField templateField : templateFields) {
            if (!templateField.getIsVisible()) {
                continue; // Skip invisible fields
            }

            FieldDefinitionMaster fieldDef = templateField.getField();
            String fieldName = fieldDef.getFieldName();
            Object value = employeeData.get(fieldName);

            // Determine if field is required (template override or default)
            boolean isRequired = templateField.getIsRequired() != null
                    ? templateField.getIsRequired()
                    : fieldDef.getIsRequiredByDefault();

            // Get validation rules (merge template override with base definition)
            Map<String, Object> validationRules = mergeValidationRules(
                    fieldDef.getValidationRules(),
                    templateField.getValidationOverride()
            );

            // Override required status from validation rules if present
            if (validationRules != null && validationRules.containsKey("required")) {
                isRequired = (Boolean) validationRules.get("required");
            }

            // Validate required fields
            if (isRequired && (value == null || value.toString().trim().isEmpty())) {
                fieldErrors.add(new FieldValidationResult(
                        fieldName,
                        fieldDef.getFieldLabel(),
                        false,
                        fieldDef.getFieldLabel() + " is required"
                ));
                continue;
            }

            // Validate field if value is present
            if (value != null && !value.toString().trim().isEmpty()) {
                FieldValidationResult result = validateField(tenantId, fieldName, value, validationRules);
                if (!result.isValid()) {
                    fieldErrors.add(result);
                }
            }
        }

        boolean isValid = fieldErrors.isEmpty();
        String message = isValid ? "Validation successful" : "Validation failed with " + fieldErrors.size() + " error(s)";

        return new ValidationResult(isValid, fieldErrors, message);
    }

    @Override
    public FieldValidationResult validateField(String tenantId, String fieldName, Object value, Map<String, Object> validationRules) {
        if (validationRules == null || validationRules.isEmpty()) {
            return new FieldValidationResult(fieldName, fieldName, true, null);
        }

        // Get field definition for label
        FieldDefinitionMaster fieldDef = fieldDefinitionRepository.findByTenantIdAndFieldName(tenantId, fieldName)
                .orElse(null);
        String fieldLabel = fieldDef != null ? fieldDef.getFieldLabel() : fieldName;

        String stringValue = value != null ? value.toString().trim() : "";

        // Pattern validation
        if (validationRules.containsKey("pattern") && !stringValue.isEmpty()) {
            String patternStr = (String) validationRules.get("pattern");
            if (!Pattern.matches(patternStr, stringValue)) {
                String errorMsg = validationRules.containsKey("errorMessage")
                        ? (String) validationRules.get("errorMessage")
                        : fieldLabel + " format is invalid";
                return new FieldValidationResult(fieldName, fieldLabel, false, errorMsg);
            }
        }

        // Min length validation
        if (validationRules.containsKey("minLength")) {
            int minLength = ((Number) validationRules.get("minLength")).intValue();
            if (stringValue.length() < minLength) {
                return new FieldValidationResult(fieldName, fieldLabel, false,
                        fieldLabel + " must be at least " + minLength + " characters");
            }
        }

        // Max length validation
        if (validationRules.containsKey("maxLength")) {
            int maxLength = ((Number) validationRules.get("maxLength")).intValue();
            if (stringValue.length() > maxLength) {
                return new FieldValidationResult(fieldName, fieldLabel, false,
                        fieldLabel + " must not exceed " + maxLength + " characters");
            }
        }

        // Min value validation (for numbers)
        if (validationRules.containsKey("min") && fieldDef != null &&
                ("number".equals(fieldDef.getFieldType()) || "decimal".equals(fieldDef.getDataType()))) {
            try {
                BigDecimal numValue = new BigDecimal(stringValue);
                BigDecimal minValue = new BigDecimal(validationRules.get("min").toString());
                if (numValue.compareTo(minValue) < 0) {
                    return new FieldValidationResult(fieldName, fieldLabel, false,
                            fieldLabel + " must be at least " + minValue);
                }
            } catch (NumberFormatException e) {
                return new FieldValidationResult(fieldName, fieldLabel, false,
                        fieldLabel + " must be a valid number");
            }
        }

        // Max value validation (for numbers)
        if (validationRules.containsKey("max") && fieldDef != null &&
                ("number".equals(fieldDef.getFieldType()) || "decimal".equals(fieldDef.getDataType()))) {
            try {
                BigDecimal numValue = new BigDecimal(stringValue);
                BigDecimal maxValue = new BigDecimal(validationRules.get("max").toString());
                if (numValue.compareTo(maxValue) > 0) {
                    return new FieldValidationResult(fieldName, fieldLabel, false,
                            fieldLabel + " must not exceed " + maxValue);
                }
            } catch (NumberFormatException e) {
                return new FieldValidationResult(fieldName, fieldLabel, false,
                        fieldLabel + " must be a valid number");
            }
        }

        // Date validation
        if (fieldDef != null && "date".equals(fieldDef.getFieldType())) {
            try {
                LocalDate.parse(stringValue);
            } catch (DateTimeParseException e) {
                return new FieldValidationResult(fieldName, fieldLabel, false,
                        fieldLabel + " must be a valid date");
            }
        }

        return new FieldValidationResult(fieldName, fieldLabel, true, null);
    }

    @Override
    public Map<String, Map<String, Object>> getTemplateValidationRules(String tenantId, Long templateId) {
        log.debug("Getting validation rules for template: {}", templateId);

        List<EmployeeTemplateField> templateFields = templateFieldRepository.findByTemplateId(templateId);

        return templateFields.stream()
                .filter(EmployeeTemplateField::getIsVisible)
                .collect(Collectors.toMap(
                        tf -> tf.getField().getFieldName(),
                        tf -> {
                            Map<String, Object> rules = mergeValidationRules(
                                    tf.getField().getValidationRules(),
                                    tf.getValidationOverride()
                            );
                            // Add required status
                            if (rules == null) {
                                rules = new HashMap<>();
                            }
                            boolean isRequired = tf.getIsRequired() != null
                                    ? tf.getIsRequired()
                                    : tf.getField().getIsRequiredByDefault();
                            rules.put("required", isRequired);
                            rules.put("fieldLabel", tf.getField().getFieldLabel());
                            rules.put("fieldType", tf.getField().getFieldType());
                            rules.put("dataType", tf.getField().getDataType());
                            rules.put("helpText", tf.getField().getHelpText());
                            rules.put("placeholderText", tf.getField().getPlaceholderText());
                            if (tf.getField().getDropdownOptions() != null && !tf.getField().getDropdownOptions().isEmpty()) {
                                rules.put("dropdownOptions", tf.getField().getDropdownOptions());
                            }
                            return rules;
                        }
                ));
    }

    /**
     * Merge validation rules with template overrides taking precedence
     */
    private Map<String, Object> mergeValidationRules(Map<String, Object> baseRules, Map<String, Object> overrides) {
        Map<String, Object> merged = new HashMap<>();
        if (baseRules != null) {
            merged.putAll(baseRules);
        }
        if (overrides != null) {
            merged.putAll(overrides);
        }
        return merged;
    }
}
