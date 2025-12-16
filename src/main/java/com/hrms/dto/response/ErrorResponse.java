package com.hrms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard error response format for all API errors.
 *
 * Format:
 * {
 *   "errors": [
 *     {
 *       "code": "INVALID_CREDENTIALS",
 *       "message": "Invalid username or password",
 *       "status": 401
 *     }
 *   ],
 *   "timestamp": "2025-12-16T21:30:00+00:00",
 *   "path": "/api/auth/login"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private List<ErrorDetail> errors;
    private OffsetDateTime timestamp;
    private String path;
    private String traceId;

    /**
     * Nested class for individual error details.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String code;
        private String message;
        private int status;
        private String field;
        private Object rejectedValue;
    }

    /**
     * Static factory method to create error response with single error.
     *
     * @param code Error code
     * @param message Error message
     * @param status HTTP status code
     * @return ErrorResponse with single error
     */
    public static ErrorResponse of(String code, String message, int status) {
        return ErrorResponse.builder()
            .errors(List.of(
                ErrorDetail.builder()
                    .code(code)
                    .message(message)
                    .status(status)
                    .build()
            ))
            .timestamp(OffsetDateTime.now())
            .build();
    }

    /**
     * Static factory method to create error response with multiple errors.
     *
     * @param errorDetails List of error details
     * @return ErrorResponse with multiple errors
     */
    public static ErrorResponse of(List<ErrorDetail> errorDetails) {
        return ErrorResponse.builder()
            .errors(errorDetails)
            .timestamp(OffsetDateTime.now())
            .build();
    }

    /**
     * Static factory method to create error response with path information.
     *
     * @param code Error code
     * @param message Error message
     * @param status HTTP status code
     * @param path Request path
     * @return ErrorResponse with error and path
     */
    public static ErrorResponse of(String code, String message, int status, String path) {
        return ErrorResponse.builder()
            .errors(List.of(
                ErrorDetail.builder()
                    .code(code)
                    .message(message)
                    .status(status)
                    .build()
            ))
            .timestamp(OffsetDateTime.now())
            .path(path)
            .build();
    }

    /**
     * Add another error to existing response.
     *
     * @param errorDetail Error detail to add
     */
    public void addError(ErrorDetail errorDetail) {
        this.errors.add(errorDetail);
    }

    /**
     * Check if response has errors.
     *
     * @return true if errors list is not empty
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Get first error code.
     *
     * @return Error code of first error, or null if no errors
     */
    public String getFirstErrorCode() {
        if (hasErrors()) {
            return errors.get(0).getCode();
        }
        return null;
    }

    /**
     * Get HTTP status code from first error.
     *
     * @return HTTP status code, or 500 if no errors
     */
    public int getHttpStatus() {
        if (hasErrors()) {
            return errors.get(0).getStatus();
        }
        return 500;
    }
}
