package com.hrms.graphql.input;

import lombok.Data;

@Data
public class CityInput {
    private String tenantId;
    private String cityName;
    private String state;
    private String pincode;
    private Boolean isActive = true;
}
