package com.hrms.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/**
 * Utility class for generating and validating Nano ID format tenant identifiers.
 *
 * Tenant IDs are globally unique, URL-safe, 21-character identifiers using Nano ID format.
 * This replaces the need for UUIDs while providing better performance and shorter strings.
 *
 * Format: A-Za-z0-9_- (21 characters)
 * Examples: V1StGXR8_Z5jdHi6B-myT, U2TdIXS9_Z5jdHi6B-myU
 */
public class TenantIdGenerator {

    private static final int NANO_ID_LENGTH = 21;
    private static final String NANO_ID_PATTERN = "^[A-Za-z0-9_-]{21}$";

    /**
     * Generates a new unique Nano ID format tenant identifier.
     *
     * @return A 21-character globally unique identifier
     */
    public static String generate() {
        return NanoIdUtils.randomNanoId();
    }

    /**
     * Validates that a given string is a valid Nano ID format tenant identifier.
     *
     * @param tenantId The tenant identifier to validate
     * @return true if valid Nano ID format, false otherwise
     */
    public static boolean isValid(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return false;
        }
        return tenantId.matches(NANO_ID_PATTERN);
    }

    /**
     * Validates that a tenant ID has the correct length (21 characters).
     *
     * @param tenantId The tenant identifier to validate
     * @return true if length is exactly 21, false otherwise
     */
    public static boolean isValidLength(String tenantId) {
        return tenantId != null && tenantId.length() == NANO_ID_LENGTH;
    }
}
