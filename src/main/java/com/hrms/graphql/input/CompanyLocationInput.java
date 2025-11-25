package com.hrms.graphql.input;

import lombok.Data;

@Data
public class CompanyLocationInput {
    private String type;
    private String name;
    private String code;
    private String addressLine1;
    private String addressLine2;
    private String state;
    private String city;
    private String pincode;

    // Location Statutory
    private String esiNumber;
    private String pfNumber;
    private String ptNumber;
    private String gstin;
    private String licenseNumber;

    // Contact
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Boolean isActive = true;
}
