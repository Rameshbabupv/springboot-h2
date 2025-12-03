package com.hrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for Employee creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    private String empId;

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 100, message = "Employee name must be between 2 and 100 characters")
    private String employeeName;

    private String gender;
    private String dateOfBirth;

    @NotBlank(message = "Date of joining is required")
    private String dateOfJoin;

    @Size(max = 15, message = "Mobile number must not exceed 15 characters")
    private String mobileNo;

    @Email(message = "Invalid email format")
    private String emailId;

    private String bloodGroup;
    private String maritalStatus;

    private Long departmentId;
    private Long designationId;
    private Long reportingManagerId;

    private BigDecimal basicSalary;
    private BigDecimal grossSalary;
    private BigDecimal ctc;

    @Size(max = 12, message = "Aadhar number must not exceed 12 characters")
    private String aadharNo;

    @Size(max = 10, message = "PAN must not exceed 10 characters")
    private String panNo;

    private String uan;

    @Builder.Default
    private Boolean coverPf = false;
    private String pfNumber;

    @Builder.Default
    private Boolean coverEsi = false;
    private String esiNumber;

    private String employeeStatus;

    @Builder.Default
    private Boolean isActive = true;
}
