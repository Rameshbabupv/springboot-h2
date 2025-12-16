package com.hrms.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Input DTO for salary component (used in both employee salary structure and templates)
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryComponentInput {

    private Long payheadId;
    private String valueType;  // FIXED or PERCENTAGE
    private BigDecimal fixedAmount;
    private BigDecimal percentage;
}
