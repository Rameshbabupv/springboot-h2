package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of Excel attendance import operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {
    private Integer importedCount;
    private Integer skippedCount;
    private List<ImportError> errors;
    private Long importLogId;
}
