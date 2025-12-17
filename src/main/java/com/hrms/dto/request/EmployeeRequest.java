package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Employee Request DTO - Flat structure for GraphQL input
 * Contains all 78 fields with 13 required fields marked with validation annotations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    // =====================================================
    // TENANT (Required)
    // =====================================================
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    // =====================================================
    // ORGANIZATIONAL ASSIGNMENT (Required: 6 fields)
    // =====================================================
    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Location is required")
    private Long locationId;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Designation is required")
    private Long designationId;

    @NotNull(message = "Job Function is required")
    private Long jobFunctionId;

    @NotNull(message = "Employment Type is required")
    private Long employmentTypeId;

    // Optional organizational fields
    private Long divisionId;
    private Long sectionId;
    private Long gradeId;
    private Long reportingManagerId;

    // =====================================================
    // EMPLOYMENT DETAILS (Required: 3 fields - Identity)
    // =====================================================
    @NotBlank(message = "Employee ID is required")
    private String empId;

    @NotBlank(message = "Employee Name is required")
    private String employeeName;

    @NotNull(message = "Date of Joining is required")
    private String dateOfJoin; // String for GraphQL (ISO 8601 format: yyyy-MM-dd)

    // Optional employment fields
    private String dateOfConfirm;
    private String dateOfRetirement;
    private String employeeStatus;
    private String experience;
    private String sourceOfHire;
    private String noticePeriod;

    // =====================================================
    // PERSONAL INFORMATION (Required: 2 fields - Statutory)
    // =====================================================
    @NotNull(message = "Date of Birth is required")
    private String dateOfBirth; // String for GraphQL (ISO 8601 format: yyyy-MM-dd)

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female, or Other")
    private String gender;

    // Optional personal fields
    private String fatherName;
    // NOTE: age is NOT in DTO - it is calculated from dateOfBirth by backend via calculateAge() method
    // Do NOT add age field here as it will overwrite the calculated value
    private String bloodGroup;
    private String maritalStatus;
    private String religion;
    private String graduation;

    // =====================================================
    // CONTACT INFORMATION (All Optional)
    // =====================================================
    private String address1;
    private String address2;
    private Long stateId;
    private Long cityId;
    private String pincode;

    @Pattern(regexp = "^[0-9]{10}$|^$", message = "Mobile number must be 10 digits")
    private String mobileNo;

    private String emailId;
    private String officialEmailId;

    @Pattern(regexp = "^[0-9]{10}$|^$", message = "Emergency number must be 10 digits")
    private String emergencyNoOne;

    @Pattern(regexp = "^[0-9]{10}$|^$", message = "Emergency number must be 10 digits")
    private String emergencyNoTwo;

    // =====================================================
    // COMPENSATION & PAYROLL (All Optional)
    // =====================================================
    private BigDecimal wages;
    private BigDecimal grossAmount;
    private BigDecimal ctc;
    private BigDecimal takeHome;
    private String effectFromSalary;
    private Boolean fetchFromTemplate;
    private Long templateId;

    // Banking details
    private String bankAccountNo;
    private String bankName;
    private String bankBranch;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$|^$", message = "IFSC code must be 11 characters (e.g., SBIN0001234)")
    private String ifscCode;

    @Pattern(regexp = "Bank|Cash|Cheque|^$", message = "Payment mode must be Bank, Cash, or Cheque")
    private String paymentMode;

    // =====================================================
    // STATUTORY DOCUMENTS (Required: 2 fields)
    // =====================================================
    @NotBlank(message = "Aadhaar Number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar must be 12 digits")
    private String aadharNo;

    @NotBlank(message = "PAN Number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN format invalid (e.g., ABCDE1234F)")
    private String panNo;

    // Optional statutory fields
    private String passportNo;
    private String dlNo;

    // PF Details
    private Boolean coverPf;
    private String uanNo;
    private String pfCode;
    private String pfEnrollmentDate;

    // ESI Details
    private Boolean coverEsi;
    private String esiCode;
    private String insuranceNo;

    // =====================================================
    // ADDITIONAL DETAILS (All Optional)
    // =====================================================
    private String shiftOrBatch;
    private Long shiftId;
    private Long employeeBatchId;
    private Boolean compOff;
    private Boolean otIncentive;
    private BigDecimal otAmount;
    private Boolean attIncentive;
    private Boolean shiftIncentive;
    private Long routeId;
    private BigDecimal km;
    private String ext;
    private String seatingLocation;

    // =====================================================
    // AUDIT FIELDS
    // =====================================================
    private Long createdBy;
    private Long updatedBy;
}
