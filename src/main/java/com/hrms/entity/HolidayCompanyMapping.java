package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Junction table for Holiday to Company mapping.
 */
@Entity
@Table(name = "holiday_company_mappings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_holiday_company", columnNames = {"holiday_id", "company_id"})
    },
    indexes = {
        @Index(name = "idx_holiday_company_tenant", columnList = "tenant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayCompanyMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_id", nullable = false)
    private Holiday holiday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Constructor for easy creation
    public HolidayCompanyMapping(Holiday holiday, Company company) {
        this.holiday = holiday;
        this.company = company;
        this.tenantId = holiday.getTenantId();
    }
}
