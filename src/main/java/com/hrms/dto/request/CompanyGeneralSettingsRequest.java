package com.hrms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Company General Settings configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGeneralSettingsRequest {

    // Organizational Structure Flags
    private Boolean enableDivisions;
    private Boolean enableDepartment;
    private Boolean enableSection;
    private Boolean enableGrade;

    // System Configuration
    private String currency;
    private String dateFormat;
    private String timeZone;
    private String financialYearStart;
    private String language;
}
