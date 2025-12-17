package com.hrms.exception;

import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Exception thrown when employee validation fails with multiple field-level errors
 * Aggregates all validation errors to be returned to the client in a single response
 */
@Getter
public class EmployeeValidationException extends ValidationException {

    private final List<FieldError> fieldErrors;

    /**
     * Constructor with aggregated field errors
     *
     * @param message The overall error message
     * @param fieldErrors List of field-level validation errors
     */
    public EmployeeValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : List.of();
    }

    /**
     * Constructor with aggregated field errors and cause
     *
     * @param message The overall error message
     * @param fieldErrors List of field-level validation errors
     * @param cause The underlying cause
     */
    public EmployeeValidationException(String message, List<FieldError> fieldErrors, Throwable cause) {
        super(message, cause);
        this.fieldErrors = fieldErrors != null ? fieldErrors : List.of();
    }

    /**
     * Get count of validation errors
     *
     * @return Number of field errors
     */
    public int getErrorCount() {
        return fieldErrors.size();
    }

    /**
     * Check if there are any validation errors
     *
     * @return true if fieldErrors is not empty
     */
    public boolean hasErrors() {
        return !fieldErrors.isEmpty();
    }

    /**
     * Get field names that have validation errors
     *
     * @return List of field names with errors
     */
    public List<String> getErrorFieldNames() {
        return fieldErrors.stream()
            .map(FieldError::getFieldName)
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "EmployeeValidationException{" +
            "message='" + getMessage() + '\'' +
            ", errorCount=" + getErrorCount() +
            ", fieldErrors=" + fieldErrors +
            '}';
    }

    /**
     * Represents a single field-level validation error
     */
    @Getter
    public static class FieldError {
        private final String fieldName;      // e.g., "dateOfJoin"
        private final String fieldLabel;     // e.g., "Date of Joining"
        private final String errorMessage;   // e.g., "Date of Joining must be in format yyyy-MM-dd"
        private final Object rejectedValue;  // e.g., "2024-13-45"

        /**
         * Constructor for field error
         *
         * @param fieldName The name of the field
         * @param fieldLabel The display label of the field
         * @param errorMessage The validation error message
         * @param rejectedValue The value that failed validation
         */
        public FieldError(String fieldName, String fieldLabel, String errorMessage, Object rejectedValue) {
            this.fieldName = Objects.requireNonNull(fieldName, "fieldName cannot be null");
            this.fieldLabel = Objects.requireNonNull(fieldLabel, "fieldLabel cannot be null");
            this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
            this.rejectedValue = rejectedValue;
        }

        @Override
        public String toString() {
            return "FieldError{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldLabel='" + fieldLabel + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", rejectedValue=" + rejectedValue +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldError that = (FieldError) o;
            return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(fieldLabel, that.fieldLabel) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(rejectedValue, that.rejectedValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldName, fieldLabel, errorMessage, rejectedValue);
        }
    }
}
