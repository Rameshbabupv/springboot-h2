package com.hrms.graphql.input;

import lombok.Data;

@Data
public class CompanyBankAccountInput {
    private String beneficiaryName;
    private String accountName;
    private String bankName;
    private String branchName;
    private String accountNumber;
    private String ifscCode;
    private String accountType;
    private Boolean isPrimary = false;
    private Boolean isActive = true;
}
