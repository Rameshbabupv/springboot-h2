package com.hrms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Utility class for password operations
 * - Password hashing with BCrypt (cost factor 12)
 * - Password verification
 * - Temporary password generation
 */
@Component
public class PasswordUtil {

    private static final int BCRYPT_STRENGTH = 12;
    private final BCryptPasswordEncoder encoder;
    private final Random random;

    public PasswordUtil() {
        this.encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH, new SecureRandom());
        this.random = new SecureRandom();
    }

    /**
     * Hash password using BCrypt
     *
     * @param rawPassword Plain text password
     * @return Hashed password
     */
    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verify password against hash
     *
     * @param rawPassword Plain text password
     * @param hashedPassword Hashed password
     * @return true if password matches
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Generate temporary password
     * Format: UpperCase + LowerCase + Digits + Special + Random
     *
     * @return Temporary password (12 characters)
     */
    public String generateTemporaryPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@$!%*?&";
        String all = upper + lower + digits + special;

        StringBuilder password = new StringBuilder();

        // Ensure at least one of each required character type
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fill remaining characters randomly
        for (int i = 4; i < 12; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Shuffle string characters
     */
    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * Validate password complexity
     *
     * @param password Password to validate
     * @return true if password meets complexity requirements
     */
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            return false;
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "@$!%*?&".indexOf(ch) >= 0);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
