package com.hrms.graphql.input;

import lombok.Data;

@Data
public class SectionInput {
    private String tenantId;
    private Long departmentId;
    private String name;
    private String code;
    private String description;
    private Boolean isActive = true;
}
