package com.hrms.exception;

/**
 * Custom exception for authentication-related errors.
 *
 * This exception is thrown when authentication fails, including:
 * - Invalid credentials (wrong username/password)
 * - Duplicate user/email during signup
 * - User not found
 * - Invalid token
 * - Token expired
 */
public class AuthenticationException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;
    private final String details;

    /**
     * Constructor with error code and HTTP status.
     *
     * @param errorCode Standard error code (e.g., "INVALID_CREDENTIALS", "EMAIL_EXISTS")
     * @param message Human-readable error message
     * @param httpStatus HTTP status code (e.g., 401, 409)
     */
    public AuthenticationException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = message;
    }

    /**
     * Constructor with error code, message, HTTP status, and details.
     *
     * @param errorCode Standard error code
     * @param message Human-readable error message
     * @param httpStatus HTTP status code
     * @param details Additional error details
     */
    public AuthenticationException(String errorCode, String message, int httpStatus, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    /**
     * Constructor with message and HTTP status (errorCode will be set to generic code).
     *
     * @param message Human-readable error message
     * @param httpStatus HTTP status code
     */
    public AuthenticationException(String message, int httpStatus) {
        super(message);
        this.errorCode = "AUTHENTICATION_ERROR";
        this.httpStatus = httpStatus;
        this.details = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Standard error codes for authentication failures.
     */
    public enum ErrorCode {
        INVALID_CREDENTIALS("INVALID_CREDENTIALS", 401),
        USER_EXISTS("USER_EXISTS", 409),
        EMAIL_EXISTS("EMAIL_EXISTS", 409),
        USER_NOT_FOUND("USER_NOT_FOUND", 404),
        USERNAME_TAKEN("USERNAME_TAKEN", 409),
        INVALID_PASSWORD("INVALID_PASSWORD", 400),
        TOKEN_EXPIRED("TOKEN_EXPIRED", 401),
        INVALID_TOKEN("INVALID_TOKEN", 401),
        AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", 401);

        private final String code;
        private final int status;

        ErrorCode(String code, int status) {
            this.code = code;
            this.status = status;
        }

        public String getCode() {
            return code;
        }

        public int getStatus() {
            return status;
        }
    }
}
