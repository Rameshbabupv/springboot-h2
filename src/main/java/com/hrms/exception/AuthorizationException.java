package com.hrms.exception;

/**
 * Custom exception for authorization-related errors.
 *
 * This exception is thrown when a user lacks permission to access a resource, including:
 * - Company access denied (companyId not in user's accessible companies)
 * - Account locked (too many failed login attempts)
 * - Account inactive
 * - Insufficient permissions/role
 * - Resource forbidden
 */
public class AuthorizationException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;
    private final String details;

    /**
     * Constructor with error code and HTTP status.
     *
     * @param errorCode Standard error code (e.g., "COMPANY_ACCESS_DENIED", "ACCOUNT_LOCKED")
     * @param message Human-readable error message
     * @param httpStatus HTTP status code (typically 403)
     */
    public AuthorizationException(String errorCode, String message, int httpStatus) {
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
    public AuthorizationException(String errorCode, String message, int httpStatus, String details) {
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
    public AuthorizationException(String message, int httpStatus) {
        super(message);
        this.errorCode = "AUTHORIZATION_ERROR";
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
     * Standard error codes for authorization failures.
     */
    public enum ErrorCode {
        COMPANY_ACCESS_DENIED("COMPANY_ACCESS_DENIED", 403),
        ACCOUNT_LOCKED("ACCOUNT_LOCKED", 403),
        ACCOUNT_INACTIVE("ACCOUNT_INACTIVE", 403),
        INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", 403),
        INSUFFICIENT_ROLE("INSUFFICIENT_ROLE", 403),
        RESOURCE_FORBIDDEN("RESOURCE_FORBIDDEN", 403),
        UNAUTHORIZED("UNAUTHORIZED", 403),
        AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", 403);

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
