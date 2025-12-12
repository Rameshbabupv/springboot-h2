package com.hrms.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA256 hash calculation.
 * Used for idempotent deduplication of punch records (ADR-002).
 *
 * @since 2.0 (ADR-002)
 */
@Slf4j
public class SHA256Calculator {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * Calculate SHA256 hash of input string.
     *
     * @param input String to hash (e.g., device_data CSV, API payload)
     * @return Lowercase hex string (64 characters)
     * @throws IllegalArgumentException if input is null
     * @throws RuntimeException if SHA-256 algorithm not available (should never happen in Java 8+)
     */
    public static String calculate(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null for SHA256 calculation");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Should never happen (SHA-256 is mandatory in Java 8+)
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Convert byte array to lowercase hex string.
     *
     * @param bytes Byte array
     * @return Hex string (lowercase)
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Verify if a string is a valid SHA256 hash (64 lowercase hex chars).
     *
     * @param hash String to verify
     * @return true if valid, false otherwise
     */
    public static boolean isValidSHA256(String hash) {
        return hash != null && hash.matches("^[a-f0-9]{64}$");
    }

    /**
     * Calculate SHA256 for device_data CSV format.
     * Helper method for consistent formatting.
     *
     * @param tenantId Tenant ID
     * @param companyId Company ID
     * @param locationId Location ID
     * @param deviceLogId Device log ID
     * @param userId User ID (biometric)
     * @param logDate Log date string
     * @param direction Direction (IN/OUT)
     * @param deviceId Device ID
     * @return SHA256 hash
     */
    public static String calculateForDeviceData(
            String tenantId, String companyId, String locationId,
            String deviceLogId, String userId, String logDate,
            String direction, String deviceId) {
        String deviceData = String.join(",",
                tenantId, companyId, locationId, deviceLogId,
                userId, logDate, direction, deviceId);
        return calculate(deviceData);
    }
}
