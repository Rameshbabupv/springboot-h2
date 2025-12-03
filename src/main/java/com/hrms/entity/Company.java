package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    // Company Identity
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", length = 10)
    private String shortName;

    @Column(length = 50)
    private String industry;

    @Column(name = "industry_description", length = 255)
    private String industryDescription;

    @Column(name = "company_type", length = 50)
    private String companyType = "Private Limited";

    @Column(columnDefinition = "TEXT")
    private String logo;

    // Registered Address
    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(length = 100)
    private String country = "India";

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(length = 10)
    private String pincode;

    // Contact Information
    @Column(name = "primary_phone", length = 15)
    private String primaryPhone;

    @Column(name = "alternate_phone", length = 15)
    private String alternatePhone;

    @Email
    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String website;

    // Primary Contact Person
    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_designation", length = 100)
    private String contactDesignation;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    // Internal
    @Column(name = "admin_notes", length = 500)
    private String adminNotes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Relations
    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CompanyStatutory statutory;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CompanyGeneralSettings generalSettings;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CompanyLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CompanyBankAccount> bankAccounts = new ArrayList<>();

    // Audit
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Helper methods
    public void setStatutory(CompanyStatutory statutory) {
        if (statutory == null) {
            if (this.statutory != null) {
                this.statutory.setCompany(null);
            }
        } else {
            statutory.setCompany(this);
        }
        this.statutory = statutory;
    }

    public void setGeneralSettings(CompanyGeneralSettings generalSettings) {
        if (generalSettings == null) {
            if (this.generalSettings != null) {
                this.generalSettings.setCompany(null);
            }
        } else {
            generalSettings.setCompany(this);
        }
        this.generalSettings = generalSettings;
    }

    public void addLocation(CompanyLocation location) {
        locations.add(location);
        location.setCompany(this);
    }

    public void removeLocation(CompanyLocation location) {
        locations.remove(location);
        location.setCompany(null);
    }

    public void addBankAccount(CompanyBankAccount bankAccount) {
        bankAccounts.add(bankAccount);
        bankAccount.setCompany(this);
    }

    public void removeBankAccount(CompanyBankAccount bankAccount) {
        bankAccounts.remove(bankAccount);
        bankAccount.setCompany(null);
    }
}
