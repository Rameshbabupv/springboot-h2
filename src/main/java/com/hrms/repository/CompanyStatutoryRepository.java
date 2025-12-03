package com.hrms.repository;

import com.hrms.entity.CompanyStatutory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyStatutoryRepository extends JpaRepository<CompanyStatutory, Long> {

    Optional<CompanyStatutory> findByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);

    void deleteByCompanyId(Long companyId);
}
