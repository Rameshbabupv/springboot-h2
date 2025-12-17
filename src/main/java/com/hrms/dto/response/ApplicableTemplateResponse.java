package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for applicable employee template query
 * Contains template metadata and field configuration for employee creation/edit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableTemplateResponse {
    // Template identification
    private Long templateId;                    // Database primary key
    private String templateName;                // Display name
    private String templateCode;                // Unique code for template
    private String description;                 // Optional description

    // Template priority and configuration
    private Integer priority;                   // Priority level for template matching
    private Integer sectionCount;               // Total number of sections in template

    // Field configuration (for Employment Detail section)
    private List<String> requiredFields;        // Fields that must be filled
    private List<String> optionalFields;        // Fields that are optional
    private List<String> customFields;          // Custom fields defined in template

    // Template sections summary
    private List<TemplateSectionSummary> sections;  // List of template sections with field counts
}
