package com.hrms.graphql.input;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeInput {
    private String tenantId;
    private Long companyId;
    private Long locationId;
    private String empId;
    private String employeeName;
    private String gender;
    private String dateOfBirth;
    private String dateOfJoin;
    private String mobileNo;
    private String emailId;
    private String bloodGroup;
    private String maritalStatus;
    private Long divisionId;
    private Long departmentId;
    private Long sectionId;
    private Long designationId;
    private Long gradeId;
    private Long jobFunctionId;
    private Long employmentTypeId;
    private Long reportingManagerId;
    private BigDecimal basicSalary;
    private BigDecimal grossSalary;
    private BigDecimal ctc;
    private String aadharNo;
    private String panNo;
    private String uan;
    private Boolean coverPf = false;
    private String pfNumber;
    private Boolean coverEsi = false;
    private String esiNumber;
    private String employeeStatus = "Active";
    private Boolean isActive = true;
}
