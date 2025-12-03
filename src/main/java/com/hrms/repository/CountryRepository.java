package com.hrms.repository;

import com.hrms.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    List<Country> findByTenantId(String tenantId);

    List<Country> findByTenantIdAndIsActiveTrue(String tenantId);

    @Query("SELECT c FROM Country c WHERE c.tenantId = :tenantId AND UPPER(c.code) = UPPER(:code)")
    Optional<Country> findByTenantIdAndCodeIgnoreCase(@Param("tenantId") String tenantId, @Param("code") String code);

    @Query("SELECT c FROM Country c WHERE c.tenantId = :tenantId AND LOWER(c.name) = LOWER(:name)")
    Optional<Country> findByTenantIdAndNameIgnoreCase(@Param("tenantId") String tenantId, @Param("name") String name);

    List<Country> findByIsActiveTrue();

    @Query("SELECT c FROM Country c WHERE c.tenantId = :tenantId AND c.isActive = true " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Country> searchCountries(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
