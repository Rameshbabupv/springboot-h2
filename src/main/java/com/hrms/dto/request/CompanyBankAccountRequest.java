package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Company Bank Account operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBankAccountRequest {

    @NotBlank(message = "Beneficiary name is required")
    @Size(max = 200, message = "Beneficiary name must not exceed 200 characters")
    private String beneficiaryName;

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    private String branchName;

    @NotBlank(message = "Account number is required")
    @Size(max = 18, message = "Account number must not exceed 18 characters")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC format")
    private String ifscCode;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @Builder.Default
    private Boolean isPrimary = false;

    @Builder.Default
    private Boolean isActive = true;
}
