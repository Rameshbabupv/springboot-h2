package com.hrms.graphql.input;

import com.hrms.enums.MissedPunchType;
import com.hrms.enums.RegularizationType;
import lombok.Data;

/**
 * GraphQL input for submitting AttendanceRegularization.
 */
@Data
public class RegularizationInput {
    private Long employeeId;                        // For self-service - employee's own ID
    private Long dailyAttendanceId;
    private RegularizationType regularizationType;
    private String regularizationDate;
    private MissedPunchType missedPunchType;        // Which punch was missed (IN/OUT/BOTH)
    private String originalPunchIn;                 // For reference
    private String originalPunchOut;
    private String regularizedPunchIn;
    private String regularizedPunchOut;
    private String reason;
}
