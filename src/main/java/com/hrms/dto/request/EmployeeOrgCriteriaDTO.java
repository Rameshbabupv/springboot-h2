package com.hrms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for organizational criteria to find applicable employee template
 * Contains all 9 organizational dimensions used for template matching
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeOrgCriteriaDTO {
    // 6 Required organizational criteria
    private Long companyId;         // Company dimension
    private Long locationId;        // Location dimension
    private Long departmentId;      // Department dimension
    private Long designationId;     // Designation dimension
    private Long jobFunctionId;     // Job Function dimension
    private Long employmentTypeId;  // Employment Type dimension

    // 3 Optional organizational criteria
    private Long divisionId;        // Division dimension (optional)
    private Long sectionId;         // Section dimension (optional)
    private Long gradeId;           // Grade dimension (optional)
}
