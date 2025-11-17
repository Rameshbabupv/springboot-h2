package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @Column(length = 100)
    private String industry;

    @Column(unique = true, nullable = false)
    private String companyName;

    @Column(length = 100)
    private String shortName;

    @Column(length = 500)
    private String logoUrl;

    private String addressLine1;
    private String addressLine2;
    private String country;
    private String state;
    private String city;

    @Column(length = 20)
    private String pincode;

    @Column(length = 20)
    private String primaryPhone;

    private String email;
    private String website;

    @Column(unique = true, length = 50)
    private String gstNumber;

    @Column(unique = true, length = 20)
    private String pan;

    @Column(unique = true, length = 20)
    private String tan;

    @Column(length = 50)
    private String cin;

    private LocalDate incorporationDate;

    @Column(length = 50)
    private String companyType;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
