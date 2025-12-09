package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error details for a failed bulk attendance entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEntryError {
    private Long employeeId;
    private String date;
    private String message;
}
