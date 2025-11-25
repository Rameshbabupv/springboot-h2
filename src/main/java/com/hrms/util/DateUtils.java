package com.hrms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date operations.
 */
public final class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

    private DateUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Parse a date string to LocalDate.
     * @param dateString the date string in yyyy-MM-dd format
     * @return LocalDate or null if parsing fails
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse a datetime string to LocalDateTime.
     * @param dateTimeString the datetime string in yyyy-MM-dd HH:mm:ss format
     * @return LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Format LocalDate to string.
     * @param date the LocalDate to format
     * @return formatted date string or null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime to string.
     * @param dateTime the LocalDateTime to format
     * @return formatted datetime string or null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Check if a date string is valid.
     * @param dateString the date string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDate(String dateString) {
        return parseDate(dateString) != null;
    }

    /**
     * Get current date as string.
     * @return current date in yyyy-MM-dd format
     */
    public static String getCurrentDateString() {
        return formatDate(LocalDate.now());
    }

    /**
     * Get current datetime as string.
     * @return current datetime in yyyy-MM-dd HH:mm:ss format
     */
    public static String getCurrentDateTimeString() {
        return formatDateTime(LocalDateTime.now());
    }
}
