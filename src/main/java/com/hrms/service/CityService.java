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

    List<City> getCitiesByState(String state);

    List<City> getActiveCities();

    City createCity(CityRequest request);

    City updateCity(Long id, CityRequest request);

    void deleteCity(Long id);

    boolean existsById(Long id);
}
