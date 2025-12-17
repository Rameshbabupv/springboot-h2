package com.hrms.repository;

import com.hrms.entity.CompanyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Long> {

    List<CompanyLocation> findByCompanyId(Long companyId);

    List<CompanyLocation> findByCompanyIdAndIsActiveTrue(Long companyId);

    Optional<CompanyLocation> findByCompanyIdAndCode(Long companyId, String code);

    List<CompanyLocation> findByCompanyIdAndType(Long companyId, String type);

    boolean existsByCompanyIdAndCode(Long companyId, String code);

    void deleteByCompanyId(Long companyId);

    // Scope-aware query - filters by company AND allowed location IDs
    List<CompanyLocation> findByCompanyIdAndIdInAndIsActiveTrue(Long companyId, List<Long> ids);
}
