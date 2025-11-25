package com.hrms.graphql.input;

import lombok.Data;

@Data
public class CompanyGeneralSettingsInput {
    // Organizational Structure
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
