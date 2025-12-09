package com.hrms.graphql.input;

import com.hrms.enums.AttendanceStatus;
import lombok.Data;

/**
 * GraphQL input for bulk manual attendance entry.
 */
@Data
public class BulkAttendanceEntryInput {
    private Long employeeId;
    private String date;
    private String punchIn;
    private String punchOut;
    private AttendanceStatus status;
    private String remarks;
}
