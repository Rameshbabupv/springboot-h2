package com.hrms.repository;

import com.hrms.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByTenantId(String tenantId);

    Optional<Company> findByCode(String code);

    Optional<Company> findByName(String name);

    Optional<Company> findByTenantIdAndCode(String tenantId, String code);

    List<Company> findByIsActiveTrue();

    List<Company> findByTenantIdAndIsActiveTrue(String tenantId);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.statutory LEFT JOIN FETCH c.generalSettings WHERE c.id = :id")
    Optional<Company> findByIdWithDetails(@Param("id") Long id);

    boolean existsByCode(String code);

    boolean existsByTenantIdAndCode(String tenantId, String code);
}
