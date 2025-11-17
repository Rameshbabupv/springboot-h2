package com.hrms.graphql.input;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompanyInput {
    private String tenantId;
    private String industry;
    private String companyName;
    private String shortName;
    private String logoUrl;
    private String addressLine1;
    private String addressLine2;
    private String country;
    private String state;
    private String city;
    private String pincode;
    private String primaryPhone;
    private String email;
    private String website;
    private String gstNumber;
    private String pan;
    private String tan;
    private String cin;
    private String incorporationDate;
    private String companyType;
    private Boolean isActive = true;
}
