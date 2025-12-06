package com.hrms.graphql.input;

import com.hrms.enums.RegularizationType;
import lombok.Data;

/**
 * GraphQL input for submitting AttendanceRegularization.
 */
@Data
public class RegularizationInput {
    private Long dailyAttendanceId;
    private RegularizationType regularizationType;
    private String regularizationDate;
    private String regularizedPunchIn;
    private String regularizedPunchOut;
    private String reason;
}
