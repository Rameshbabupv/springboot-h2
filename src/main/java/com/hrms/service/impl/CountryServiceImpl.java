package com.hrms.service.impl;

import com.hrms.dto.request.CountryRequest;
import com.hrms.entity.Country;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.CountryRepository;
import com.hrms.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Country operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    public List<Country> getAllCountries() {
        log.debug("Fetching all countries");
        return countryRepository.findAll();
    }

    @Override
    public Country getCountryById(Long id) {
        log.debug("Fetching country with id: {}", id);
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", id));
    }

    @Override
    public List<Country> getCountriesByTenant(String tenantId) {
        log.debug("Fetching countries for tenant: {}", tenantId);
        return countryRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Country> getActiveCountriesByTenant(String tenantId) {
        log.debug("Fetching active countries for tenant: {}", tenantId);
        return countryRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    public List<Country> getActiveCountries() {
        log.debug("Fetching active countries");
        return countryRepository.findByIsActiveTrue();
    }

    @Override
    public List<Country> searchCountries(String tenantId, String searchTerm) {
        log.debug("Searching countries for tenant: {} with term: {}", tenantId, searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveCountriesByTenant(tenantId);
        }
        return countryRepository.searchCountries(tenantId, searchTerm);
    }

    @Override
    @Transactional
    public Country createCountry(CountryRequest request) {
        log.debug("Creating new country: {}", request.getName());

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<Country> existingCode = countryRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (existingCode.isPresent()) {
            throw new DuplicateResourceException("Country", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<Country> existingName = countryRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (existingName.isPresent()) {
            throw new DuplicateResourceException("Country", "name", request.getName());
        }

        Country country = mapToEntity(request);
        Country saved = countryRepository.save(country);

        log.info("Created country with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Country updateCountry(Long id, CountryRequest request) {
        log.debug("Updating country with id: {}", id);

        Country existing = getCountryById(id);

        // Auto-convert code to uppercase
        String upperCode = request.getCode().toUpperCase();
        request.setCode(upperCode);

        // Case-insensitive duplicate check for code
        Optional<Country> duplicateCode = countryRepository
                .findByTenantIdAndCodeIgnoreCase(request.getTenantId(), request.getCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getId().equals(id)) {
            throw new DuplicateResourceException("Country", "code", request.getCode());
        }

        // Case-insensitive duplicate check for name
        Optional<Country> duplicateName = countryRepository
                .findByTenantIdAndNameIgnoreCase(request.getTenantId(), request.getName());
        if (duplicateName.isPresent() && !duplicateName.get().getId().equals(id)) {
            throw new DuplicateResourceException("Country", "name", request.getName());
        }

        updateEntityFromRequest(existing, request);
        Country updated = countryRepository.save(existing);

        log.info("Updated country with id: {}", id);
        return updated;
    }

    @Override
    @Transactional
    public void deleteCountry(Long id) {
        log.debug("Deleting country with id: {}", id);

        if (!countryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Country", "id", id);
        }
        countryRepository.deleteById(id);

        log.info("Deleted country with id: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return countryRepository.existsById(id);
    }

    private Country mapToEntity(CountryRequest request) {
        Country country = new Country();
        country.setTenantId(request.getTenantId());
        country.setName(request.getName());
        country.setCode(request.getCode());
        country.setCurrencyCode(request.getCurrencyCode());
        country.setPhoneCode(request.getPhoneCode());
        country.setDescription(request.getDescription());
        country.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        country.setCreatedBy(request.getCreatedBy());
        return country;
    }

    private void updateEntityFromRequest(Country country, CountryRequest request) {
        country.setTenantId(request.getTenantId());
        country.setName(request.getName());
        country.setCode(request.getCode());
        country.setCurrencyCode(request.getCurrencyCode());
        country.setPhoneCode(request.getPhoneCode());
        country.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            country.setIsActive(request.getIsActive());
        }
        country.setUpdatedBy(request.getUpdatedBy());
    }
}
