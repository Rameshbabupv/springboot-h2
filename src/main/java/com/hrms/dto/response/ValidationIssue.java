package com.hrms.dto.response;

import com.hrms.enums.IssueSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details of a validation issue during Excel import validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationIssue {
    private Integer rowNumber;
    private String column;
    private String value;
    private String message;
    private IssueSeverity severity;
}
