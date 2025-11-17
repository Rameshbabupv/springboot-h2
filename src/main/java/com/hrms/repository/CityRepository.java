package com.hrms.repository;

import com.hrms.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByTenantId(String tenantId);

    Optional<City> findByTenantIdAndCityNameAndState(String tenantId, String cityName, String state);

    List<City> findByState(String state);

    List<City> findByIsActiveTrue();
}
