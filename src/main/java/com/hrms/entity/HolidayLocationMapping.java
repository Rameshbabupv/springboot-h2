package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Junction table for Holiday to Location mapping.
 */
@Entity
@Table(name = "holiday_location_mappings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_holiday_location", columnNames = {"holiday_id", "location_id"})
    },
    indexes = {
        @Index(name = "idx_holiday_location_tenant", columnList = "tenant_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayLocationMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_id", nullable = false)
    private Holiday holiday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private CompanyLocation location;

    // Constructor for easy creation
    public HolidayLocationMapping(Holiday holiday, CompanyLocation location) {
        this.holiday = holiday;
        this.location = location;
        this.tenantId = holiday.getTenantId();
    }
}
