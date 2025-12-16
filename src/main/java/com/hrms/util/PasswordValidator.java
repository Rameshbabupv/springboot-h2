package com.hrms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password validation utility enforcing security requirements.
 *
 * Password requirements (aligned with OWASP guidelines):
 * - Minimum 8 characters
 * - At least 1 uppercase letter (A-Z)
 * - At least 1 lowercase letter (a-z)
 * - At least 1 digit (0-9)
 * - At least 1 special character (@$!%*?&)
 *
 * This ensures strong passwords that are resistant to brute force and dictionary attacks.
 */
public class PasswordValidator {

    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]");
    private static final Pattern INVALID_CHAR_PATTERN = Pattern.compile("[^\\w!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]");

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    /**
     * Validates a password against security requirements.
     *
     * @param password The password to validate
     * @return ValidationResult containing success status and error messages
     */
    public static ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new ValidationResult(false, errors);
        }

        // Check length
        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password.length() > MAX_LENGTH) {
            errors.add("Password must not exceed " + MAX_LENGTH + " characters");
        }

        // Check for uppercase
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter (A-Z)");
        }

        // Check for lowercase
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter (a-z)");
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one digit (0-9)");
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one special character (!@#$%^&*()_+-=[]{};\\':\",.<>?)");
        }

        // Check for invalid/control characters
        if (INVALID_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Password contains invalid characters");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Simple method to check if password is valid (returns boolean only).
     *
     * @param password The password to validate
     * @return true if password meets all requirements, false otherwise
     */
    public static boolean isValid(String password) {
        return validate(password).isValid();
    }

    /**
     * Nested class to hold password validation results.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "";
            }
            return String.join(", ", errors);
        }
    }
}
