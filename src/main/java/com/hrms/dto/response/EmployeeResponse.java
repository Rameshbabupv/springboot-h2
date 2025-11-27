package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee Response DTO - Nested structure for GraphQL output
 * Contains all fields with nested objects for better API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    // =====================================================
    // PRIMARY KEY & TENANT
    // =====================================================
    private Long id;
    private String tenantId;

    // =====================================================
    // ORGANIZATIONAL ASSIGNMENT (Nested Objects)
    // =====================================================
    private CompanyResponse company;
    private CompanyLocationResponse location;
    private DepartmentResponse department;
    private DesignationResponse designation;
    private JobFunctionResponse jobFunction;
    private EmploymentTypeResponse employmentType;
    
    // Optional organizational
    private DivisionResponse division;
    private SectionResponse section;
    private GradeResponse grade;
    private EmployeeResponse reportingManager; // Self-reference for nested manager info

    // =====================================================
    // EMPLOYMENT DETAILS
    // =====================================================
    private String empId;
    private String employeeName;
    private LocalDate dateOfJoin;
    private LocalDate dateOfConfirm;
    private LocalDate dateOfRetirement;
    private String employeeStatus;
    private String experience;
    private String sourceOfHire;
    private String noticePeriod;

    // =====================================================
    // PERSONAL INFORMATION
    // =====================================================
    private LocalDate dateOfBirth;
    private String gender;
    private String fatherName;
    private Integer age;
    private String bloodGroup;
    private String maritalStatus;
    private String religion;
    private String graduation;

    // =====================================================
    // CONTACT INFORMATION
    // =====================================================
    private String address1;
    private String address2;
    private StateResponse state;
    private CityResponse city;
    private String pincode;
    private String mobileNo;
    private String emailId;
    private String officialEmailId;
    private String emergencyNoOne;
    private String emergencyNoTwo;

    // =====================================================
    // COMPENSATION & PAYROLL
    // =====================================================
    private BigDecimal wages;
    private BigDecimal grossAmount;
    private BigDecimal ctc;
    private BigDecimal takeHome;
    private LocalDate effectFromSalary;
    private Boolean fetchFromTemplate;
    private Long templateId; // Will be nested object when PayTemplate is implemented

    // Banking details
    private String bankAccountNo;
    private String bankName;
    private String bankBranch;
    private String ifscCode;
    private String paymentMode;

    // =====================================================
    // STATUTORY DOCUMENTS
    // =====================================================
    private String aadharNo;
    private String panNo;
    private String passportNo;
    private String dlNo;

    // PF Details
    private Boolean coverPf;
    private String uanNo;
    private String pfCode;
    private LocalDate pfEnrollmentDate;

    // ESI Details
    private Boolean coverEsi;
    private String esiCode;
    private String insuranceNo;

    // =====================================================
    // ADDITIONAL DETAILS
    // =====================================================
    private String shiftOrBatch;
    private Long shiftId; // Will be nested object when Shift is implemented
    private Long employeeBatchId; // Will be nested object when Batch is implemented
    private Boolean compOff;
    private Boolean otIncentive;
    private BigDecimal otAmount;
    private Boolean attIncentive;
    private Boolean shiftIncentive;
    private Long routeId; // Will be nested object when Route is implemented
    private BigDecimal km;
    private String ext;
    private String seatingLocation;

    // =====================================================
    // AUDIT FIELDS
    // =====================================================
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
