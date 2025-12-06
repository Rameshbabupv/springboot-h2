package com.hrms.graphql.input;

import com.hrms.enums.ShiftType;
import com.hrms.enums.TimingMode;
import lombok.Data;

import java.util.List;

/**
 * GraphQL input for creating/updating Shift.
 */
@Data
public class ShiftInput {
    private String code;
    private String name;
    private String description;
    private ShiftType shiftType;
    private TimingMode timingMode;
    private String startTime;
    private String endTime;
    private Double workingHours;
    private Integer breakDurationMinutes;
    private Boolean isNightShift;
    private Boolean isOtEligible;
    private String firstHalfEnd;
    private String secondHalfStart;
    private Boolean isActive;
    private Integer displayOrder;
    private List<ShiftBreakInput> breaks;
}
