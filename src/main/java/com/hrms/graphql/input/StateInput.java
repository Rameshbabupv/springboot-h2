package com.hrms.graphql.input;

import lombok.Data;

@Data
public class StateInput {
    private String tenantId;
    private String name;
    private String code;
    private String description;
    private Boolean isActive = true;
}
