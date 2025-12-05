package com.hrms.graphql.input;

import com.hrms.enums.IncentiveCalcType;
import lombok.Data;

/**
 * GraphQL input for PolicyIncentive.
 */
@Data
public class PolicyIncentiveInput {
    private String payheadCode;
    private IncentiveCalcType calcType;
    private Double overrideValue;
    private Boolean isActive;
}
