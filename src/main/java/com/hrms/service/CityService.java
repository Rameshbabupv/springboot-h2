package com.hrms.service;

import com.hrms.dto.request.CityRequest;
import com.hrms.entity.City;

import java.util.List;

/**
 * Service interface for City operations.
 */
public interface CityService {

    List<City> getAllCities();

    City getCityById(Long id);

    List<City> getCitiesByTenant(String tenantId);

    List<City> getActiveCitiesByTenant(String tenantId);

    List<City> getActiveCities();

    List<City> getCitiesByState(Long stateId);

    List<City> getCitiesByTenantAndState(String tenantId, Long stateId);

    List<City> getCitiesByCountry(Long countryId);

    List<City> getCitiesByTenantAndCountry(String tenantId, Long countryId);

    List<City> searchCities(String tenantId, String searchTerm);

    City createCity(CityRequest request);

    City updateCity(Long id, CityRequest request);

    void deleteCity(Long id);

    boolean existsById(Long id);
}
