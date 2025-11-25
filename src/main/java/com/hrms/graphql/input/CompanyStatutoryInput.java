package com.hrms.graphql.input;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompanyStatutoryInput {
    // Tax Identifiers
    private String pan;
    private String tan;
    private String cin;
    private String lin;
    private String gstin;

    // PF Configuration
    private Boolean pfEnabled;
    private String pfAccountNumber;
    private BigDecimal pfCeiling;
    private BigDecimal pfEmployeeRate;
    private BigDecimal pfEmployerRate;
    private BigDecimal pfEmployerEpfRate;
    private BigDecimal pfEmployerEpsRate;

    // ESI Configuration
    private Boolean esiEnabled;
    private String esiNumber;
    private BigDecimal esiCeiling;
    private BigDecimal esiEmployeeRate;
    private BigDecimal esiEmployerRate;

    // PT Configuration
    private Boolean ptEnabled;
    private String ptState;
    private String ptRegistrationNumber;
    private String ptRegistrationDate;
    private String ptValidUpto;

    // HR Config
    private Integer retirementAge;
    private String tdsType;
    private Boolean allowTdsOverride;
    private String applicableActs;
}
