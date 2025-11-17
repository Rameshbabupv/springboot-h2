package com.hrms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_tenant_company", columnList = "tenantId,companyId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(unique = true, nullable = false, length = 50)
    private String empId;

    @Column(nullable = false)
    private String employeeName;

    @Column(length = 20)
    private String gender;

    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate dateOfJoin;

    @Column(length = 20)
    private String mobileNo;

    private String emailId;

    @Column(length = 10)
    private String bloodGroup;

    @Column(length = 20)
    private String maritalStatus;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne
    @JoinColumn(name = "reporting_manager_id")
    private Employee reportingManager;

    @Column(precision = 10, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal grossSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal ctc;

    @Column(length = 12)
    private String aadharNo;

    @Column(length = 10)
    private String panNo;

    @Column(length = 50)
    private String uan;

    @Column(nullable = false)
    private Boolean coverPf = false;

    @Column(length = 50)
    private String pfNumber;

    @Column(nullable = false)
    private Boolean coverEsi = false;

    @Column(length = 50)
    private String esiNumber;

    @Column(length = 20)
    private String employeeStatus = "Active";

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
