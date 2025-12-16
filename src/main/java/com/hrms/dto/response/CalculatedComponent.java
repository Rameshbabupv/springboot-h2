package com.hrms.dto.response;

import com.hrms.enums.PayheadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for individual component breakdown in batch salary calculation
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatedComponent {

    private Long payheadId;
    private String payheadName;
    private String payheadCode;
    private PayheadType payheadType;
    private String valueType;  // FIXED or PERCENTAGE
    private BigDecimal inputValue;  // What user entered (amount or percentage)
    private BigDecimal calculatedAmount;  // Computed final amount
    private String calculationBase;  // BASIC, GROSS, CTC (for percentage types)
}
