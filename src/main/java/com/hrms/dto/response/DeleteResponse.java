package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response for delete operations.
 * Used by delete mutations to indicate success/failure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteResponse {
    private Boolean success;    // Deletion operation succeeded
    private String message;     // Optional message (error details, confirmation, etc.)
}
