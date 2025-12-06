package com.hrms.graphql.input;

import lombok.Data;

@Data
public class LeavePolicyInput {
    private String code;
    private String name;
    private String description;
    private String leaveYearStart;
    private String status;
    private String effectiveFrom;
    private String effectiveTo;
    private Boolean isDefault;
}
