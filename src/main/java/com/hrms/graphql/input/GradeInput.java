package com.hrms.graphql.input;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeInput {
    private String tenantId;
    private String name;
    private String code;
    private Integer level;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String description;
    private Boolean isActive = true;
}
