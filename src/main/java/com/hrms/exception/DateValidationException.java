package com.hrms.exception;

/**
 * Exception thrown when date validation fails
 * Includes field name and rejected value for error responses
 */
public class DateValidationException extends ValidationException {

    private final String fieldName;
    private final String invalidValue;

    /**
     * Constructor for date validation exception
     *
     * @param message The error message
     * @param fieldName The name of the field that failed validation
     * @param invalidValue The value that was rejected
     */
    public DateValidationException(String message, String fieldName, String invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    /**
     * Constructor for date validation exception with cause
     *
     * @param message The error message
     * @param fieldName The name of the field that failed validation
     * @param invalidValue The value that was rejected
     * @param cause The underlying cause
     */
    public DateValidationException(String message, String fieldName, String invalidValue, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    /**
     * Get the field name that failed validation
     *
     * @return Field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get the invalid value that was rejected
     *
     * @return Invalid value
     */
    public String getInvalidValue() {
        return invalidValue;
    }
}
