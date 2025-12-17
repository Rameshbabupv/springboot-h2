package com.hrms.validator;

import com.hrms.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator for Personal & Contact tab field formats and values
 * Ensures data integrity for phone numbers, emails, pincodes, blood groups, and marital status
 */
@Slf4j
@Component
public class PersonalContactFieldValidator {

    /**
     * Validate phone number format (exactly 10 digits, numeric only)
     * Applicable to: mobileNo, emergencyNoOne, emergencyNoTwo
     *
     * @param phoneNo The phone number to validate
     * @param fieldName The field name for error messages (e.g., "Mobile Number", "Emergency Contact 1")
     * @throws ValidationException if format is invalid
     */
    public void validatePhoneNumber(String phoneNo, String fieldName) {
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            log.debug("Skipping phone validation for {} - field is empty", fieldName);
            return; // Optional fields should not throw error if empty
        }

        // Remove spaces and hyphens if present
        String cleanedNumber = phoneNo.replaceAll("[\\s-]", "");

        if (!cleanedNumber.matches("^[0-9]{10}$")) {
            log.error("Phone validation failed for {}: {} does not match pattern ^[0-9]{{10}}$", fieldName, phoneNo);
            throw new ValidationException(fieldName + " must be exactly 10 digits and numeric only (provided: " + phoneNo + ")");
        }

        log.debug("Phone validation passed for {}: {}", fieldName, phoneNo);
    }

    /**
     * Validate email format
     * Applicable to: emailId, officialEmailId
     *
     * @param email The email to validate
     * @param fieldName The field name for error messages
     * @throws ValidationException if format is invalid
     */
    public void validateEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty()) {
            log.debug("Skipping email validation for {} - field is empty", fieldName);
            return; // Handled by @NotEmpty in DTO
        }

        // RFC 5322 simplified regex - matches most common email formats
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        if (!email.matches(emailRegex)) {
            log.error("Email validation failed for {}: {} does not match email pattern", fieldName, email);
            throw new ValidationException(fieldName + " must be valid email format (provided: " + email + ")");
        }

        log.debug("Email validation passed for {}: {}", fieldName, email);
    }

    /**
     * Validate pincode format (exactly 6 digits, numeric only for India)
     * Applicable to: pincode
     *
     * @param pincode The pincode to validate
     * @param fieldName The field name for error messages
     * @throws ValidationException if format is invalid
     */
    public void validatePincode(String pincode, String fieldName) {
        if (pincode == null || pincode.trim().isEmpty()) {
            log.debug("Skipping pincode validation - field is empty");
            return; // Required field should be handled by required field validation
        }

        // Remove spaces if present
        String cleanedPincode = pincode.replaceAll("\\s", "");

        if (!cleanedPincode.matches("^[0-9]{6}$")) {
            log.error("Pincode validation failed: {} does not match pattern ^[0-9]{{6}}$", pincode);
            throw new ValidationException(fieldName + " must be exactly 6 digits (provided: " + pincode + ")");
        }

        log.debug("Pincode validation passed: {}", pincode);
    }

    /**
     * Validate blood group value against predefined list
     * Applicable to: bloodGroup
     *
     * @param bloodGroup The blood group to validate
     * @throws ValidationException if not valid blood group
     */
    public void validateBloodGroup(String bloodGroup) {
        if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
            log.debug("Skipping blood group validation - field is empty (optional field)");
            return; // Optional field
        }

        String[] validGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        boolean isValid = false;

        for (String group : validGroups) {
            if (group.equals(bloodGroup)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            log.error("Blood group validation failed: {} is not a valid blood group", bloodGroup);
            throw new ValidationException("Blood Group must be one of: A+, A-, B+, B-, O+, O-, AB+, AB- (provided: " + bloodGroup + ")");
        }

        log.debug("Blood group validation passed: {}", bloodGroup);
    }

    /**
     * Validate marital status value against predefined list
     * Applicable to: maritalStatus
     *
     * @param maritalStatus The marital status to validate
     * @throws ValidationException if not valid status
     */
    public void validateMaritalStatus(String maritalStatus) {
        if (maritalStatus == null || maritalStatus.trim().isEmpty()) {
            log.debug("Skipping marital status validation - field is empty (optional field)");
            return; // Optional field
        }

        String[] validStatuses = {"Single", "Married", "Divorced", "Widowed"};
        boolean isValid = false;

        for (String status : validStatuses) {
            if (status.equals(maritalStatus)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            log.error("Marital status validation failed: {} is not a valid status", maritalStatus);
            throw new ValidationException("Marital Status must be one of: Single, Married, Divorced, Widowed (provided: " + maritalStatus + ")");
        }

        log.debug("Marital status validation passed: {}", maritalStatus);
    }
}
