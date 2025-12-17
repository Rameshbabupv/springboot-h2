package com.hrms.graphql.input;

import com.hrms.enums.CalculationType;
import com.hrms.enums.PayheadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Input DTO for creating/updating Payhead Master
 *
 * @author Claude Sonnet 4.5
 * @since December 16, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayheadInput {

    private String payheadName;
    private String payheadCode;
    private PayheadType payheadType;
    private String payheadCategory;

    private CalculationType calculationType;
    private String calculationBase;
    private BigDecimal defaultValue;
    private String formula;

    private Boolean isTaxable;
    private Boolean affectsPf;
    private Boolean affectsEsi;
    private Boolean affectsGratuity;
    private Boolean affectsLwf;
    private Boolean affectsPt;  // Professional Tax

    // Rules & Limits
    private String roundingRule;  // NONE, ROUND, FLOOR, CEIL, ROUND_10, ROUND_100
    private BigDecimal minValue;  // Minimum amount
    private BigDecimal maxValue;  // Maximum amount

    // Conditional Application
    private String applicableCondition;  // e.g., "GROSS <= 21000"

    // Additional Information
    private String description;

    private Integer displayOrder;
    private Boolean showInPayslip;
    private Boolean isMandatory;
    private Boolean isActive;
}
