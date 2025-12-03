package com.hrms.repository;

import com.hrms.entity.CompanyBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Long> {

    List<CompanyBankAccount> findByCompanyId(Long companyId);

    List<CompanyBankAccount> findByCompanyIdAndIsActiveTrue(Long companyId);

    Optional<CompanyBankAccount> findByCompanyIdAndIsPrimaryTrue(Long companyId);

    Optional<CompanyBankAccount> findByCompanyIdAndAccountNumber(Long companyId, String accountNumber);

    boolean existsByCompanyIdAndAccountNumber(Long companyId, String accountNumber);

    @Modifying
    @Query("UPDATE CompanyBankAccount b SET b.isPrimary = false WHERE b.company.id = :companyId")
    void resetPrimaryForCompany(@Param("companyId") Long companyId);

    void deleteByCompanyId(Long companyId);
}
