package com.hrms.graphql.input;

import lombok.Data;

@Data
public class EmploymentTypeInput {
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private Boolean isActive = true;
}
