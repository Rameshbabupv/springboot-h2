package com.hrms.repository;

import com.hrms.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByTenantId(String tenantId);

    Optional<Company> findByCompanyName(String companyName);

    List<Company> findByIsActiveTrue();
}
