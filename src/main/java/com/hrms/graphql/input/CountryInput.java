package com.hrms.graphql.input;

import lombok.Data;

@Data
public class CountryInput {
    private String tenantId;
    private String name;
    private String code;
    private String currencyCode;
    private String phoneCode;
    private String description;
    private Boolean isActive = true;
    private String createdBy;
    private String updatedBy;
}
