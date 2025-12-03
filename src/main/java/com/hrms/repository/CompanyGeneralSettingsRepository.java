package com.hrms.repository;

import com.hrms.entity.CompanyGeneralSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyGeneralSettingsRepository extends JpaRepository<CompanyGeneralSettings, Long> {

    Optional<CompanyGeneralSettings> findByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);

    void deleteByCompanyId(Long companyId);
}
