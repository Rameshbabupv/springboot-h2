package com.hrms.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for Company Statutory configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatutoryRequest {

    // Tax Identification Numbers
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format")
    private String pan;

    @Pattern(regexp = "^[A-Z]{4}[0-9]{5}[A-Z]$", message = "Invalid TAN format")
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

    // HR Policies
    private Integer retirementAge;

    // TDS Configuration
    private String tdsType;
    private Boolean allowTdsOverride;

    // Applicable Acts
    private List<String> applicableActs;
}
