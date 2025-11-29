package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_statutory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatutory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Tax Identification Numbers
    @Column(length = 10)
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format")
    private String pan;

    @Column(length = 10)
    @Pattern(regexp = "^[A-Z]{4}[0-9]{5}[A-Z]$", message = "Invalid TAN format")
    private String tan;

    @Column(length = 21)
    private String cin;

    @Column(length = 21)
    private String lin;

    @Column(length = 15)
    private String gstin;

    // Provident Fund (PF) Configuration
    @Column(name = "pf_enabled")
    private Boolean pfEnabled = false;

    @Column(name = "pf_account_number", length = 25)
    private String pfAccountNumber;

    @Column(name = "pf_ceiling", precision = 10, scale = 2)
    private BigDecimal pfCeiling = new BigDecimal("15000");

    @Column(name = "pf_employee_rate", precision = 5, scale = 2)
    private BigDecimal pfEmployeeRate = new BigDecimal("12.00");

    @Column(name = "pf_employer_rate", precision = 5, scale = 2)
    private BigDecimal pfEmployerRate = new BigDecimal("12.00");

    @Column(name = "pf_employer_epf_rate", precision = 5, scale = 2)
    private BigDecimal pfEmployerEpfRate = new BigDecimal("3.67");

    @Column(name = "pf_employer_eps_rate", precision = 5, scale = 2)
    private BigDecimal pfEmployerEpsRate = new BigDecimal("8.33");

    // Employee State Insurance (ESI) Configuration
    @Column(name = "esi_enabled")
    private Boolean esiEnabled = false;

    @Column(name = "esi_number", length = 17)
    private String esiNumber;

    @Column(name = "esi_ceiling", precision = 10, scale = 2)
    private BigDecimal esiCeiling = new BigDecimal("21000");

    @Column(name = "esi_employee_rate", precision = 5, scale = 2)
    private BigDecimal esiEmployeeRate = new BigDecimal("0.75");

    @Column(name = "esi_employer_rate", precision = 5, scale = 2)
    private BigDecimal esiEmployerRate = new BigDecimal("3.25");

    // Professional Tax (PT) Configuration
    @Column(name = "pt_enabled")
    private Boolean ptEnabled = false;

    @Column(name = "pt_state", length = 50)
    private String ptState;

    @Column(name = "pt_registration_number", length = 30)
    private String ptRegistrationNumber;

    @Column(name = "pt_registration_date")
    private LocalDate ptRegistrationDate;

    @Column(name = "pt_valid_upto")
    private LocalDate ptValidUpto;

    // HR Policies
    @Column(name = "retirement_age")
    private Integer retirementAge = 60;

    // TDS Configuration
    @Column(name = "tds_type", length = 10)
    private String tdsType = "TDS";

    @Column(name = "allow_tds_override")
    private Boolean allowTdsOverride = false;

    // Applicable Acts (stored as comma-separated string for simplicity)
    @Column(name = "applicable_acts", columnDefinition = "TEXT")
    private String applicableActs;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Helper method to get applicable acts as list
    public List<String> getApplicableActsList() {
        if (applicableActs == null || applicableActs.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(applicableActs.split(","));
    }

    // Helper method to set applicable acts from list
    public void setApplicableActsList(List<String> acts) {
        if (acts == null || acts.isEmpty()) {
            this.applicableActs = null;
        } else {
            this.applicableActs = String.join(",", acts);
        }
    }
}
