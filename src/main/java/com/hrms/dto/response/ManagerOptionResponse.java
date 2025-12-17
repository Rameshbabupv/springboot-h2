package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for manager selection in Employee Creation form
 * Contains minimal manager information for dropdown/selection list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerOptionResponse {

    private Long id;                    // Employee ID (database primary key)
    private String empId;               // Employee ID (business identifier)
    private String employeeName;        // Display name
    private DesignationResponse designation;  // Manager's designation
    private DepartmentResponse department;    // Manager's department
    private String role;                // User role (MANAGER, ADMIN, etc.)
    private Boolean isActive;           // Whether user account is active
}
