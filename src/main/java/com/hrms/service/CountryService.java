package com.hrms.service;

import com.hrms.dto.request.CountryRequest;
import com.hrms.entity.Country;

import java.util.List;

/**
 * Service interface for Country operations.
 */
public interface CountryService {

    List<Country> getAllCountries();

    Country getCountryById(Long id);

    List<Country> getCountriesByTenant(String tenantId);

    List<Country> getActiveCountriesByTenant(String tenantId);

    List<Country> getActiveCountries();

    List<Country> searchCountries(String tenantId, String searchTerm);

    Country createCountry(CountryRequest request);

    Country updateCountry(Long id, CountryRequest request);

    void deleteCountry(Long id);

    boolean existsById(Long id);
}
