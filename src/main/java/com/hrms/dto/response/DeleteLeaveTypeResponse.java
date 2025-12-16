package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response type for deleteLeaveType mutation.
 * Indicates whether the deletion was successful and provides additional context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteLeaveTypeResponse {
    private Boolean success;         // Deletion operation succeeded
    private String message;          // User-friendly message
    private Boolean hardDeleted;     // true = permanently deleted, false = soft deleted (is_active = false)
    private String reason;           // Reason for soft delete (e.g., "IN_USE - Referenced in 5 leave policies")
}
