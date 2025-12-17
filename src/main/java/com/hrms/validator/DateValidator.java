package com.hrms.validator;

import com.hrms.exception.DateValidationException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator for date parsing and relationship validation
 * Ensures strict date validation with meaningful error messages
 *
 * Supported Format: ISO-8601 (yyyy-MM-dd)
 */
@Slf4j
public class DateValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MINIMUM_AGE = 18;

    /**
     * Parse and validate a date string
     * Throws DateValidationException on invalid format or business rule violation
     *
     * @param dateStr The date string to parse (expected format: yyyy-MM-dd)
     * @param fieldLabel The field name for error message
     * @return Parsed LocalDate
     * @throws DateValidationException if date is invalid or parsing fails
     */
    public static LocalDate parseAndValidateDate(String dateStr, String fieldLabel)
            throws DateValidationException {

        // Handle null and empty strings
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            LocalDate parsedDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            log.debug("Successfully parsed {} date: {}", fieldLabel, parsedDate);
            return parsedDate;
        } catch (DateTimeParseException e) {
            log.error("Failed to parse {} date '{}': {}", fieldLabel, dateStr, e.getMessage());
            throw new DateValidationException(
                String.format("%s must be in valid format (yyyy-MM-dd). Invalid value: %s",
                    fieldLabel, dateStr),
                fieldLabel,
                dateStr
            );
        }
    }

    /**
     * Validate date relationships for employment dates
     * Rules:
     * - dateOfJoin <= dateOfConfirm (if confirm provided)
     * - dateOfConfirm <= dateOfRetirement (if retirement provided)
     * - dateOfJoin <= dateOfRetirement (if retirement provided)
     * - No future dates for dateOfJoin
     * - dateOfBirth at least 18 years before dateOfJoin
     *
     * @param dateOfJoin Date of joining (required, non-null)
     * @param dateOfConfirm Date of confirmation (optional, nullable)
     * @param dateOfRetirement Date of retirement (optional, nullable)
     * @param dateOfBirth Date of birth (required, non-null)
     * @throws DateValidationException if any relationship is violated
     */
    public static void validateDateRelationships(
            LocalDate dateOfJoin,
            LocalDate dateOfConfirm,
            LocalDate dateOfRetirement,
            LocalDate dateOfBirth) throws DateValidationException {

        log.debug("Validating date relationships: doj={}, doc={}, dor={}, dob={}",
            dateOfJoin, dateOfConfirm, dateOfRetirement, dateOfBirth);

        if (dateOfJoin == null) {
            throw new DateValidationException(
                "Date of Joining is required",
                "dateOfJoin",
                "null"
            );
        }

        if (dateOfBirth == null) {
            throw new DateValidationException(
                "Date of Birth is required",
                "dateOfBirth",
                "null"
            );
        }

        // Validate no future dates for dateOfJoin
        validateNoFutureDate(dateOfJoin, "Date of Joining");

        // Validate no future dates for dateOfBirth
        validateNoFutureDate(dateOfBirth, "Date of Birth");

        // Validate minimum age requirement
        validateMinimumAge(dateOfBirth, dateOfJoin);

        // Validate date relationships
        validateJoinConfirmRelationship(dateOfJoin, dateOfConfirm);
        validateConfirmRetirementRelationship(dateOfConfirm, dateOfRetirement);
        validateJoinRetirementRelationship(dateOfJoin, dateOfRetirement);

        log.info("All date relationships validated successfully");
    }

    /**
     * Validate that a date is not in the future
     *
     * @param date The date to validate
     * @param fieldLabel The field name for error message
     * @throws DateValidationException if date is in the future
     */
    private static void validateNoFutureDate(LocalDate date, String fieldLabel)
            throws DateValidationException {

        if (date.isAfter(LocalDate.now())) {
            throw new DateValidationException(
                String.format("%s cannot be a future date. Invalid value: %s",
                    fieldLabel, date),
                fieldLabel.toLowerCase().replace(" ", ""),
                date.toString()
            );
        }
    }

    /**
     * Validate that employee is at least 18 years old at joining
     *
     * @param dateOfBirth Date of birth
     * @param dateOfJoin Date of joining
     * @throws DateValidationException if employee is younger than 18 at joining
     */
    private static void validateMinimumAge(LocalDate dateOfBirth, LocalDate dateOfJoin)
            throws DateValidationException {

        Period agePeriod = Period.between(dateOfBirth, dateOfJoin);

        if (agePeriod.getYears() < MINIMUM_AGE) {
            throw new DateValidationException(
                String.format("Employee must be at least %d years old at joining. " +
                    "Date of Birth: %s, Date of Joining: %s",
                    MINIMUM_AGE, dateOfBirth, dateOfJoin),
                "dateOfBirth",
                dateOfBirth.toString()
            );
        }
    }

    /**
     * Validate that dateOfJoin <= dateOfConfirm
     *
     * @param dateOfJoin Date of joining
     * @param dateOfConfirm Date of confirmation (nullable)
     * @throws DateValidationException if confirmation date is before joining date
     */
    private static void validateJoinConfirmRelationship(LocalDate dateOfJoin, LocalDate dateOfConfirm)
            throws DateValidationException {

        if (dateOfConfirm != null && dateOfConfirm.isBefore(dateOfJoin)) {
            throw new DateValidationException(
                String.format("Date of Confirmation (%s) cannot be before Date of Joining (%s)",
                    dateOfConfirm, dateOfJoin),
                "dateOfConfirm",
                dateOfConfirm.toString()
            );
        }
    }

    /**
     * Validate that dateOfConfirm <= dateOfRetirement
     *
     * @param dateOfConfirm Date of confirmation (nullable)
     * @param dateOfRetirement Date of retirement (nullable)
     * @throws DateValidationException if retirement date is before confirmation date
     */
    private static void validateConfirmRetirementRelationship(LocalDate dateOfConfirm, LocalDate dateOfRetirement)
            throws DateValidationException {

        if (dateOfConfirm != null && dateOfRetirement != null && dateOfRetirement.isBefore(dateOfConfirm)) {
            throw new DateValidationException(
                String.format("Date of Retirement (%s) cannot be before Date of Confirmation (%s)",
                    dateOfRetirement, dateOfConfirm),
                "dateOfRetirement",
                dateOfRetirement.toString()
            );
        }
    }

    /**
     * Validate that dateOfJoin <= dateOfRetirement
     *
     * @param dateOfJoin Date of joining
     * @param dateOfRetirement Date of retirement (nullable)
     * @throws DateValidationException if retirement date is before joining date
     */
    private static void validateJoinRetirementRelationship(LocalDate dateOfJoin, LocalDate dateOfRetirement)
            throws DateValidationException {

        if (dateOfRetirement != null && dateOfRetirement.isBefore(dateOfJoin)) {
            throw new DateValidationException(
                String.format("Date of Retirement (%s) cannot be before Date of Joining (%s)",
                    dateOfRetirement, dateOfJoin),
                "dateOfRetirement",
                dateOfRetirement.toString()
            );
        }
    }
}
