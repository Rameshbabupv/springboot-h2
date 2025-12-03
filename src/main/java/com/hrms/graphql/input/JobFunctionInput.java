package com.hrms.graphql.input;

import lombok.Data;

@Data
public class JobFunctionInput {
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private String functionGroup;
    private Boolean isActive = true;
    private String createdBy;
    private String updatedBy;
}
