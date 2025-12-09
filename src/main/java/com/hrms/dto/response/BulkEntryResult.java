package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of bulk manual attendance entry operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEntryResult {
    private Integer successCount;
    private Integer failedCount;
    private List<BulkEntryError> errors;
}
