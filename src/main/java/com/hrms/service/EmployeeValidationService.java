package com.hrms.service;

import java.util.List;
import java.util.Map;

/**
 * Service for validating employee data based on field definitions and template configurations
 */
public interface EmployeeValidationService {

    /**
     * Validate employee data against a specific template
     * @param tenantId The tenant ID
     * @param templateId The employee template ID
     * @param employeeData The employee data to validate
     * @return Validation result with errors if any
     */
    ValidationResult validateEmployeeData(String tenantId, Long templateId, Map<String, Object> employeeData);

    /**
     * Validate a single field value
     * @param tenantId The tenant ID
     * @param fieldName The field name
     * @param value The field value
     * @param validationRules The validation rules to apply
     * @return Validation result for the field
     */
    FieldValidationResult validateField(String tenantId, String fieldName, Object value, Map<String, Object> validationRules);

    /**
     * Get validation rules for a specific template
     * @param tenantId The tenant ID
     * @param templateId The employee template ID
     * @return Map of field names to their validation rules
     */
    Map<String, Map<String, Object>> getTemplateValidationRules(String tenantId, Long templateId);

    /**
     * Validation result container
     */
    class ValidationResult {
        private boolean valid;
        private List<FieldValidationResult> fieldErrors;
        private String message;

        public ValidationResult(boolean valid, List<FieldValidationResult> fieldErrors, String message) {
            this.valid = valid;
            this.fieldErrors = fieldErrors;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public List<FieldValidationResult> getFieldErrors() {
            return fieldErrors;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Field-level validation result
     */
    class FieldValidationResult {
        private String fieldName;
        private String fieldLabel;
        private boolean valid;
        private String errorMessage;

        public FieldValidationResult(String fieldName, String fieldLabel, boolean valid, String errorMessage) {
            this.fieldName = fieldName;
            this.fieldLabel = fieldLabel;
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldLabel() {
            return fieldLabel;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
