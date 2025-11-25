package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Basic Information
    @Column(nullable = false, length = 20)
    private String type;  // Head Office, Branch, Factory, Warehouse, Store

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    // Address
    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 10)
    private String pincode;

    // Location-Specific Statutory Registration Numbers
    @Column(name = "esi_number", length = 17)
    private String esiNumber;

    @Column(name = "pf_number", length = 25)
    private String pfNumber;

    @Column(name = "pt_number", length = 30)
    private String ptNumber;

    @Column(length = 15)
    private String gstin;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    // Site Contact Person
    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    // Status
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
