package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basic employee information for attendance capture dropdowns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBasicResponse {
    private Long id;
    private String employeeCode;
    private String fullName;        // Maps to employeeName from Employee entity
    private String designation;
    private String department;
    // Note: photoUrl not currently available in Employee entity
}
