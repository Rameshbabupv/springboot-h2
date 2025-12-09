package com.hrms.dto.response;

import com.hrms.entity.DailyAttendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Employee row for attendance grid with existing attendance info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAttendanceRow {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String designation;
    private String department;
    private DailyAttendance existingAttendance;  // null if no entry for date
}
