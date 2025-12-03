package com.hrms.repository;

import com.hrms.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByTenantId(String tenantId);

    List<City> findByTenantIdAndIsActiveTrue(String tenantId);

    List<City> findByStateId(Long stateId);

    List<City> findByTenantIdAndStateId(String tenantId, Long stateId);

    List<City> findByCountryId(Long countryId);

    List<City> findByTenantIdAndCountryId(String tenantId, Long countryId);

    @Query("SELECT c FROM City c WHERE c.tenantId = :tenantId AND c.stateId = :stateId AND c.countryId = :countryId AND UPPER(c.code) = UPPER(:code)")
    Optional<City> findByTenantIdAndStateIdAndCountryIdAndCodeIgnoreCase(
            @Param("tenantId") String tenantId,
            @Param("stateId") Long stateId,
            @Param("countryId") Long countryId,
            @Param("code") String code);

    @Query("SELECT c FROM City c WHERE c.tenantId = :tenantId AND c.stateId = :stateId AND c.countryId = :countryId AND LOWER(c.name) = LOWER(:name)")
    Optional<City> findByTenantIdAndStateIdAndCountryIdAndNameIgnoreCase(
            @Param("tenantId") String tenantId,
            @Param("stateId") Long stateId,
            @Param("countryId") Long countryId,
            @Param("name") String name);

    List<City> findByIsActiveTrue();

    @Query("SELECT c FROM City c WHERE c.tenantId = :tenantId AND c.isActive = true " +
           "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.pincode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<City> searchCities(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
