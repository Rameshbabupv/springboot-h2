package com.hrms.graphql.input;

import com.hrms.enums.AttendanceStatus;
import lombok.Data;

/**
 * Input type for updating attendance record (Time Center inline editing).
 * Used by the updateAttendanceRecord mutation.
 */
@Data
public class AttendanceUpdateInput {

    private Long attendanceId;      // Required: ID of daily_attendance record
    private String firstPunchIn;    // Optional: HH:MM format (24-hour)
    private String lastPunchOut;    // Optional: HH:MM format (24-hour)
    private AttendanceStatus status; // Optional: PRESENT, ABSENT, etc.
    private String remarks;         // Optional: Notes/comments
}
