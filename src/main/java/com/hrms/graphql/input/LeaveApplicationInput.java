package com.hrms.graphql.input;

import com.hrms.enums.LeaveDayType;
import lombok.Data;

@Data
public class LeaveApplicationInput {
    private Long leaveTypeId;
    private String fromDate;
    private String toDate;
    private LeaveDayType fromDayType;
    private LeaveDayType toDayType;
    private String reason;
}
