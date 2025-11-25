package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Company Location operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyLocationRequest {

    @NotBlank(message = "Location type is required")
    private String type;

    @NotBlank(message = "Location name is required")
    @Size(max = 100, message = "Location name must not exceed 100 characters")
    private String name;

    @Size(max = 20, message = "Location code must not exceed 20 characters")
    private String code;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Pincode is required")
    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    private String pincode;

    // Location-specific statutory numbers
    private String esiNumber;
    private String pfNumber;
    private String ptNumber;
    private String gstin;
    private String licenseNumber;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    private String contactEmail;

    @Builder.Default
    private Boolean isActive = true;
}
