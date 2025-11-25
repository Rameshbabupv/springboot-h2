package com.hrms.service.impl;

import com.hrms.dto.request.CityRequest;
import com.hrms.entity.City;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CityRepository;
import com.hrms.service.CityService;
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
    public List<City> getCitiesByState(String state) {
        log.debug("Fetching cities for state: {}", state);
        return cityRepository.findByState(state);
    }

    @Override
    public List<City> getActiveCities() {
        log.debug("Fetching active cities");
        return cityRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public City createCity(CityRequest request) {
        log.debug("Creating new city: {}", request.getCityName());

        Optional<City> existing = cityRepository.findByTenantIdAndCityNameAndState(
                request.getTenantId(), request.getCityName(), request.getState());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("City", "cityName", request.getCityName());
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

        Optional<City> duplicate = cityRepository.findByTenantIdAndCityNameAndState(
                request.getTenantId(), request.getCityName(), request.getState());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateResourceException("City", "cityName", request.getCityName());
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
        city.setCityName(request.getCityName());
        city.setState(request.getState());
        city.setPincode(request.getPincode());
        city.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return city;
    }

    private void updateEntityFromRequest(City city, CityRequest request) {
        city.setTenantId(request.getTenantId());
        city.setCityName(request.getCityName());
        city.setState(request.getState());
        city.setPincode(request.getPincode());
        if (request.getIsActive() != null) {
            city.setIsActive(request.getIsActive());
        }
    }
}
