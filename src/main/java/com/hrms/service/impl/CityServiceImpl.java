package com.hrms.service.impl;

import com.hrms.dto.request.CityRequest;
import com.hrms.entity.City;
import com.hrms.entity.State;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CityRepository;
import com.hrms.service.CityService;
import com.hrms.service.CountryService;
import com.hrms.service.StateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for City operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final StateService stateService;
    private final CountryService countryService;

    @Override
    public List<City> getAllCities() {
        log.debug("Fetching all cities");
        return cityRepository.findAll();
    }

    @Override
    public City getCityById(Long id) {
        log.debug("Fetching city with id: {}", id);
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", id));
    }

    @Override
    public List<City> getCitiesByTenant(String tenantId) {
        log.debug("Fetching cities for tenant: {}", tenantId);
        return cityRepository.findByTenantId(tenantId);
    }

    @Override
    public List<City> getActiveCitiesByTenant(String tenantId) {
        log.debug("Fetching active cities for tenant: {}", tenantId);
        return cityRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<City> getActiveCities() {
        log.debug("Fetching active cities");
        return cityRepository.findByIsActiveTrue();
    }

    @Override
    public List<City> getCitiesByState(Long stateId) {
        log.debug("Fetching cities for state: {}", stateId);
        return cityRepository.findByStateId(stateId);
    }

    @Override
    public List<City> getCitiesByTenantAndState(String tenantId, Long stateId) {
        log.debug("Fetching cities for tenant: {} and state: {}", tenantId, stateId);
        return cityRepository.findByTenantIdAndStateId(tenantId, stateId);
    }

    @Override
    public List<City> getCitiesByCountry(Long countryId) {
        log.debug("Fetching cities for country: {}", countryId);
        return cityRepository.findByCountryId(countryId);
    }

    @Override
    public List<City> getCitiesByTenantAndCountry(String tenantId, Long countryId) {
        log.debug("Fetching cities for tenant: {} and country: {}", tenantId, countryId);
        return cityRepository.findByTenantIdAndCountryId(tenantId, countryId);
    }

    @Override
    public List<City> searchCities(String tenantId, String searchTerm) {
        log.debug("Searching cities for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveCitiesByTenant(tenantId);
        }
        return cityRepository.searchCities(tenantId, searchTerm);
    }

    @Override
    @Transactional
    public City createCity(CityRequest request) {
        log.debug("Creating new city: {}", request.getName());

        // Validate that country exists
        if (!countryService.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country", "id", request.getCountryId());
        }

        // Validate that state exists
        if (!stateService.existsById(request.getStateId())) {
            throw new ResourceNotFoundException("State", "id", request.getStateId());
        }

        // Validate that state belongs to the specified country
        State state = stateService.getStateById(request.getStateId());
        if (!state.getCountryId().equals(request.getCountryId())) {
            throw new IllegalArgumentException(
                    String.format("State with ID %d does not belong to Country with ID %d",
                            request.getStateId(), request.getCountryId()));
        }

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<City> existingCode = cityRepository
                .findByTenantIdAndStateIdAndCountryIdAndCodeIgnoreCase(
                        request.getTenantId(),
                        request.getStateId(),
                        request.getCountryId(),
                        request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("City", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<City> existingName = cityRepository
                .findByTenantIdAndStateIdAndCountryIdAndNameIgnoreCase(
                        request.getTenantId(),
                        request.getStateId(),
                        request.getCountryId(),
                        request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("City", "name", request.getName());
        }

        City city = mapToEntity(request);
        City saved = cityRepository.save(city);

        log.info("Created city with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public City updateCity(Long id, CityRequest request) {
        log.debug("Updating city with id: {}", id);

        City existing = getCityById(id);

        // Validate that country exists
        if (!countryService.existsById(request.getCountryId())) {
            throw new ResourceNotFoundException("Country", "id", request.getCountryId());
        }

        // Validate that state exists
        if (!stateService.existsById(request.getStateId())) {
            throw new ResourceNotFoundException("State", "id", request.getStateId());
        }

        // Validate that state belongs to the specified country
        State state = stateService.getStateById(request.getStateId());
        if (!state.getCountryId().equals(request.getCountryId())) {
            throw new IllegalArgumentException(
                    String.format("State with ID %d does not belong to Country with ID %d",
                            request.getStateId(), request.getCountryId()));
        }

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<City> duplicateCode = cityRepository
                .findByTenantIdAndStateIdAndCountryIdAndCodeIgnoreCase(
                        request.getTenantId(),
                        request.getStateId(),
                        request.getCountryId(),
                        request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("City", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<City> duplicateName = cityRepository
                .findByTenantIdAndStateIdAndCountryIdAndNameIgnoreCase(
                        request.getTenantId(),
                        request.getStateId(),
                        request.getCountryId(),
                        request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("City", "name", request.getName());
        }

        updateEntityFromRequest(existing, request);
        City updated = cityRepository.save(existing);

        log.info("Updated city with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteCity(Long id) {
        log.debug("Deleting city with id: {}", id);

        if (!cityRepository.existsById(id)) {
            throw new ResourceNotFoundException("City", "id", id);
        }
        cityRepository.deleteById(id);

        log.info("Deleted city with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return cityRepository.existsById(id);
    }

    private City mapToEntity(CityRequest request) {
        City city = new City();
        city.setTenantId(request.getTenantId());
        city.setCountryId(request.getCountryId());
        city.setStateId(request.getStateId());
        city.setName(request.getName());
        city.setCode(request.getCode());
        city.setPincode(request.getPincode());
        city.setLatitude(request.getLatitude());
        city.setLongitude(request.getLongitude());
        city.setDescription(request.getDescription());
        city.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        city.setCreatedBy(request.getCreatedBy());
        return city;
    }

    private void updateEntityFromRequest(City city, CityRequest request) {
        city.setTenantId(request.getTenantId());
        city.setCountryId(request.getCountryId());
        city.setStateId(request.getStateId());
        city.setName(request.getName());
        city.setCode(request.getCode());
        city.setPincode(request.getPincode());
        city.setLatitude(request.getLatitude());
        city.setLongitude(request.getLongitude());
        city.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            city.setIsActive(request.getIsActive());
        }
        city.setUpdatedBy(request.getUpdatedBy());
    }
}
