package com.hrms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee Entity - Main employee master table
 * Total Fields: 78 columns
 * Required Fields: 13 (following "13 Essential Fields" recommendation)
 * Multi-tenant: Yes (tenant_id)
 */
@Entity
@Table(name = "employees",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_emp_id_tenant", columnNames = {"emp_id", "tenant_id"})
    },
    indexes = {
        @Index(name = "idx_employee_tenant", columnList = "tenant_id"),
        @Index(name = "idx_employee_company", columnList = "company_id"),
        @Index(name = "idx_employee_emp_id", columnList = "emp_id"),
        @Index(name = "idx_employee_status", columnList = "employee_status"),
        @Index(name = "idx_employee_department", columnList = "department_id"),
        @Index(name = "idx_employee_location", columnList = "location_id"),
        @Index(name = "idx_employee_designation", columnList = "designation_id"),
        @Index(name = "idx_employee_date_of_join", columnList = "date_of_join"),
        @Index(name = "idx_employee_reporting_manager", columnList = "reporting_manager_id"),
        @Index(name = "idx_tenant_company", columnList = "tenant_id, company_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    // =====================================================
    // PRIMARY KEY & TENANT
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tenant_id", nullable = false, length = 50)
    private String tenantId;

    // =====================================================
    // ORGANIZATIONAL ASSIGNMENT (Required: 6 fields)
    // =====================================================

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private CompanyLocation location;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_function_id", nullable = false)
    private JobFunction jobFunction;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employment_type_id", nullable = false)
    private EmploymentType employmentType;

    // Optional organizational fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;

    // Reporting structure (self-reference)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private Employee reportingManager;

    // =====================================================
    // EMPLOYMENT DETAILS (Required: 3 fields - Identity)
    // =====================================================

    @NotBlank
    @Column(name = "emp_id", nullable = false, length = 50)
    private String empId;

    @NotBlank
    @Column(name = "employee_name", nullable = false, length = 255)
    private String employeeName;

    @NotNull
    @Column(name = "date_of_join", nullable = false)
    private LocalDate dateOfJoin;

    // Optional employment fields
    @Column(name = "date_of_confirm")
    private LocalDate dateOfConfirm;

    @Column(name = "date_of_retirement")
    private LocalDate dateOfRetirement;

    @Column(name = "employee_status", length = 20)
    private String employeeStatus;

    @Column(name = "experience", length = 50)
    private String experience;

    @Column(name = "source_of_hire", length = 100)
    private String sourceOfHire;

    @Column(name = "notice_period", length = 50)
    private String noticePeriod;

    // =====================================================
    // PERSONAL INFORMATION (Required: 2 fields - Statutory)
    // =====================================================

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    // Optional personal fields
    @Column(name = "father_name", length = 255)
    private String fatherName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "religion", length = 50)
    private String religion;

    @Column(name = "graduation", length = 100)
    private String graduation;

    // =====================================================
    // CONTACT INFORMATION (All Optional)
    // =====================================================

    @Column(name = "address_1", length = 255)
    private String address1;

    @Column(name = "address_2", length = 255)
    private String address2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "pincode", length = 6)
    private String pincode;

    @Column(name = "mobile_no", length = 10)
    private String mobileNo;

    @Column(name = "email_id", length = 255)
    private String emailId;

    @Column(name = "official_email_id", length = 255)
    private String officialEmailId;

    @Column(name = "emergency_no_one", length = 10)
    private String emergencyNoOne;

    @Column(name = "emergency_no_two", length = 10)
    private String emergencyNoTwo;

    // =====================================================
    // COMPENSATION & PAYROLL (All Optional)
    // =====================================================

    // Salary components
    @Column(name = "wages", precision = 15, scale = 2)
    private BigDecimal wages;

    @Column(name = "gross_amount", precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "ctc", precision = 15, scale = 2)
    private BigDecimal ctc;

    @Column(name = "take_home", precision = 15, scale = 2)
    private BigDecimal takeHome;

    @Column(name = "effect_from_salary")
    private LocalDate effectFromSalary;

    // Pay template
    @Column(name = "fetch_from_template")
    private Boolean fetchFromTemplate = false;

    @Column(name = "template_id")
    private Long templateId;
    // Note: Will add @ManyToOne relationship when PayTemplate entity is created

    // Banking details (Optional - add before salary payment)
    @Column(name = "bank_account_no", length = 50)
    private String bankAccountNo;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "payment_mode", length = 20)
    private String paymentMode;

    // =====================================================
    // STATUTORY DOCUMENTS (Required: 2 fields)
    // =====================================================

    @NotBlank
    @Column(name = "aadhar_no", nullable = false, length = 12)
    private String aadharNo;

    @NotBlank
    @Column(name = "pan_no", nullable = false, length = 10)
    private String panNo;

    // Optional statutory fields
    @Column(name = "passport_no", length = 20)
    private String passportNo;

    @Column(name = "dl_no", length = 20)
    private String dlNo;

    // PF Details
    @Column(name = "cover_pf")
    private Boolean coverPf = false;

    @Column(name = "uan_no", length = 12)
    private String uanNo;

    @Column(name = "pf_code", length = 50)
    private String pfCode;

    @Column(name = "pf_enrollment_date")
    private LocalDate pfEnrollmentDate;

    // ESI Details
    @Column(name = "cover_esi")
    private Boolean coverEsi = false;

    @Column(name = "esi_code", length = 50)
    private String esiCode;

    @Column(name = "insurance_no", length = 50)
    private String insuranceNo;

    // =====================================================
    // ADDITIONAL DETAILS (All Optional)
    // =====================================================

    // Attendance & Leave
    @Column(name = "shift_or_batch", length = 1)
    private String shiftOrBatch;

    @Column(name = "shift_id")
    private Long shiftId;
    // Note: Will add @ManyToOne relationship when Shift entity is created

    @Column(name = "employee_batch_id")
    private Long employeeBatchId;
    // Note: Will add @ManyToOne relationship when Batch entity is created

    @Column(name = "comp_off")
    private Boolean compOff = false;

    // Allowances & Incentives
    @Column(name = "ot_incentive")
    private Boolean otIncentive = false;

    @Column(name = "ot_amount", precision = 10, scale = 2)
    private BigDecimal otAmount;

    @Column(name = "att_incentive")
    private Boolean attIncentive = false;

    @Column(name = "shift_incentive")
    private Boolean shiftIncentive = false;

    // Transport
    @Column(name = "route_id")
    private Long routeId;
    // Note: Will add @ManyToOne relationship when Route entity is created

    @Column(name = "km", precision = 10, scale = 2)
    private BigDecimal km;

    // Office Details
    @Column(name = "ext", length = 10)
    private String ext;

    @Column(name = "seating_location", length = 100)
    private String seatingLocation;

    // =====================================================
    // AUDIT FIELDS
    // =====================================================

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
