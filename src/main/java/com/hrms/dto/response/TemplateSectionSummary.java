package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary of a template section with field count
 * Nested within ApplicableTemplateResponse for template structure overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSectionSummary {
    private String sectionName;     // Display name of section
    private String sectionCode;     // Unique code for section (e.g., "EMPLOYMENT_DETAIL")
    private Integer fieldCount;     // Number of fields in this section
}
