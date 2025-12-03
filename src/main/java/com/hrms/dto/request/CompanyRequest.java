package com.hrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Company creation and update operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Company code is required")
    @Size(max = 20, message = "Company code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Company name must be between 2 and 200 characters")
    private String name;

    @Size(max = 10, message = "Short name must not exceed 10 characters")
    private String shortName;

    private String industry;

    @Size(max = 255, message = "Industry description must not exceed 255 characters")
    private String industryDescription;

    private String companyType;

    private String logo;

    // Registered Address
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    private String country;
    private String state;
    private String city;

    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    private String pincode;

    // Contact Information
    @Size(max = 15, message = "Primary phone must not exceed 15 characters")
    private String primaryPhone;

    @Size(max = 15, message = "Alternate phone must not exceed 15 characters")
    private String alternatePhone;

    @Email(message = "Invalid email format")
    private String email;

    private String website;

    // Primary Contact Person
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;

    @Size(max = 100, message = "Contact designation must not exceed 100 characters")
    private String contactDesignation;

    @Email(message = "Invalid contact email format")
    private String contactEmail;

    @Size(max = 15, message = "Contact phone must not exceed 15 characters")
    private String contactPhone;

    // Internal
    @Size(max = 500, message = "Admin notes must not exceed 500 characters")
    private String adminNotes;

    @Builder.Default
    private Boolean isActive = true;
}
