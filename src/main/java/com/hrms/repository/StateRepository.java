package com.hrms.repository;

import com.hrms.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    List<State> findByTenantId(String tenantId);

    List<State> findByTenantIdAndIsActiveTrue(String tenantId);

    List<State> findByCountryId(Long countryId);

    List<State> findByTenantIdAndCountryId(String tenantId, Long countryId);

    List<State> findByTenantIdAndCountryIdAndIsActiveTrue(String tenantId, Long countryId);

    @Query("SELECT s FROM State s WHERE s.tenantId = :tenantId AND s.countryId = :countryId AND UPPER(s.code) = UPPER(:code)")
    Optional<State> findByTenantIdAndCountryIdAndCodeIgnoreCase(
            @Param("tenantId") String tenantId,
            @Param("countryId") Long countryId,
            @Param("code") String code);

    @Query("SELECT s FROM State s WHERE s.tenantId = :tenantId AND s.countryId = :countryId AND LOWER(s.name) = LOWER(:name)")
    Optional<State> findByTenantIdAndCountryIdAndNameIgnoreCase(
            @Param("tenantId") String tenantId,
            @Param("countryId") Long countryId,
            @Param("name") String name);

    List<State> findByIsActiveTrue();

    @Query("SELECT s FROM State s WHERE s.tenantId = :tenantId AND s.isActive = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<State> searchStates(@Param("tenantId") String tenantId, @Param("searchTerm") String searchTerm);
}
