package com.hrms.graphql.input;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CityInput {
    private String tenantId;
    private Long countryId;
    private Long stateId;
    private String name;
    private String code;
    private String pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Boolean isActive = true;
    private String createdBy;
    private String updatedBy;
}
