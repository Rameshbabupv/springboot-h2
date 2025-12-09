package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error details for a failed Excel import row.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportError {
    private Integer rowNumber;
    private String employeeId;
    private String message;
}
