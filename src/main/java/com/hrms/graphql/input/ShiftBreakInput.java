package com.hrms.graphql.input;

import com.hrms.enums.BreakType;
import lombok.Data;

/**
 * GraphQL input for ShiftBreak.
 */
@Data
public class ShiftBreakInput {
    private BreakType breakType;
    private String name;
    private String startTime;
    private String endTime;
    private Integer durationMinutes;
    private Boolean isPaid;
}
