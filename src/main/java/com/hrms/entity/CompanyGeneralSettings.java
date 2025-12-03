package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "company_general_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGeneralSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Organizational Structure Flags
    @Column(name = "enable_divisions")
    private Boolean enableDivisions = false;

    @Column(name = "enable_department")
    private Boolean enableDepartment = true;

    @Column(name = "enable_section")
    private Boolean enableSection = false;

    @Column(name = "enable_grade")
    private Boolean enableGrade = false;

    // System Configuration
    @Column(length = 3)
    private String currency = "INR";

    @Column(name = "date_format", length = 15)
    private String dateFormat = "DD/MM/YYYY";

    @Column(name = "time_zone", length = 50)
    private String timeZone = "Asia/Kolkata";

    @Column(name = "financial_year_start", length = 15)
    private String financialYearStart = "April";

    @Column(length = 5)
    private String language = "en";

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
