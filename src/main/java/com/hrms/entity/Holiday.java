package com.hrms.entity;

import com.hrms.enums.HolidayCategory;
import com.hrms.enums.HolidayType;
import com.hrms.enums.Religion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Holiday entity - Defines holidays for attendance calculation.
 */
@Entity
@Table(name = "holidays",
    indexes = {
        @Index(name = "idx_holidays_tenant", columnList = "tenant_id"),
        @Index(name = "idx_holidays_tenant_date", columnList = "tenant_id, date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false, length = 1)
    private HolidayType holidayType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private HolidayCategory category;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Column(name = "is_ni_act_compliant")
    private Boolean isNiActCompliant = false;

    @Column(name = "is_floating")
    private Boolean isFloating = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", length = 20)
    private Religion religion;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "holiday", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HolidayCompanyMapping> companyMappings = new ArrayList<>();

    @OneToMany(mappedBy = "holiday", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HolidayLocationMapping> locationMappings = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Helper methods
    public void addCompanyMapping(HolidayCompanyMapping mapping) {
        companyMappings.add(mapping);
        mapping.setHoliday(this);
        mapping.setTenantId(this.tenantId);
    }

    public void addLocationMapping(HolidayLocationMapping mapping) {
        locationMappings.add(mapping);
        mapping.setHoliday(this);
        mapping.setTenantId(this.tenantId);
    }

    public void clearMappings() {
        companyMappings.clear();
        locationMappings.clear();
    }
}
