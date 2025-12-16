package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Input DTO for creating/updating Salary Template
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryTemplateInput {

    private String templateName;
    private String templateCode;
    private String description;

    private List<Long> gradeIds;
    private List<Long> designationIds;

    private List<SalaryComponentInput> salaryComponents;

    private Boolean isActive;
}
