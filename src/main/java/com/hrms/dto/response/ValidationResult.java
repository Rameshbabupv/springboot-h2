package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of Excel import validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private Integer totalRows;
    private Integer validRecords;
    private List<ValidationIssue> warnings;
    private List<ValidationIssue> errors;
}
