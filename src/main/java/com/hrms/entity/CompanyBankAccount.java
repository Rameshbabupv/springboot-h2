package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_bank_account", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "account_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Account Details
    @Column(name = "beneficiary_name", nullable = false, length = 200)
    private String beneficiaryName;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "account_number", nullable = false, length = 18)
    private String accountNumber;

    @Column(name = "ifsc_code", nullable = false, length = 11)
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC format")
    private String ifscCode;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;  // Current Account, Cash Credit, Overdraft

    // Flags
    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
