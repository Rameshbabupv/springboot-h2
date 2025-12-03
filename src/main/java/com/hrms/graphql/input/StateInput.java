package com.hrms.graphql.input;

import lombok.Data;

@Data
public class StateInput {
    private String tenantId;
    private Long countryId;
    private String name;
    private String code;
    private String stateCode;
    private Boolean isUnionTerritory = false;
    private String description;
    private Boolean isActive = true;
    private String createdBy;
    private String updatedBy;
}
