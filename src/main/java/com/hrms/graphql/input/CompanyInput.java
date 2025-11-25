package com.hrms.graphql.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyInput {
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    public String shortName;

    @JsonProperty("industry")
    private String industry;

    @JsonProperty("companyType")
    private String companyType;

    @JsonProperty("logoUrl")
    public String logoUrl;

    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("country")
    private String country;

    @JsonProperty("state")
    private String state;

    @JsonProperty("city")
    private String city;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("primaryPhone")
    private String primaryPhone;

    @JsonProperty("alternatePhone")
    private String alternatePhone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("website")
    private String website;

    @JsonProperty("contactName")
    private String contactName;

    @JsonProperty("contactDesignation")
    private String contactDesignation;

    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactPhone")
    private String contactPhone;

    @JsonProperty("adminNotes")
    private String adminNotes;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    // Default constructor
    public CompanyInput() {
    }

    // Getters and Setters
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) {
        System.out.println("=== SETTER: setShortName called with value: " + shortName);
        this.shortName = shortName;
    }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) {
        System.out.println("=== SETTER: setLogoUrl called with value: " + (logoUrl != null ? logoUrl.substring(0, Math.min(50, logoUrl.length())) + "..." : "null"));
        this.logoUrl = logoUrl;
    }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactDesignation() { return contactDesignation; }
    public void setContactDesignation(String contactDesignation) { this.contactDesignation = contactDesignation; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
