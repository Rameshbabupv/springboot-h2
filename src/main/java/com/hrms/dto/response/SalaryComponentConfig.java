package com.hrms.dto.response;

import com.hrms.enums.PayheadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for salary component configuration (used in templates)
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryComponentConfig {

    private Long payheadId;
    private String payheadCode;
    private String payheadName;
    private PayheadType payheadType;

    private String valueType;  // FIXED or PERCENTAGE
    private BigDecimal amount;
    private BigDecimal percentage;
    private String calculationBase;  // BASIC, GROSS, CTC
}
