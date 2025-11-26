package com.hrms.graphql.resolver;

import com.hrms.dto.request.CityRequest;
import com.hrms.entity.City;
import com.hrms.graphql.input.CityInput;
import com.hrms.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CityResolver {

    private final CityService cityService;

    @QueryMapping
    public List<City> cities() {
        return cityService.getAllCities();
    }

    @QueryMapping
    public City city(@Argument Long id) {
        return cityService.getCityById(id);
    }

    @QueryMapping
    public List<City> citiesByTenant(@Argument String tenantId) {
        return cityService.getCitiesByTenant(tenantId);
    }

    @QueryMapping
    public List<City> activeCitiesByTenant(@Argument String tenantId) {
        return cityService.getActiveCitiesByTenant(tenantId);
    }

    @QueryMapping
    public List<City> activeCities() {
        return cityService.getActiveCities();
    }

    @QueryMapping
    public List<City> citiesByState(@Argument Long stateId) {
        return cityService.getCitiesByState(stateId);
    }

    @QueryMapping
    public List<City> citiesByTenantAndState(@Argument String tenantId, @Argument Long stateId) {
        return cityService.getCitiesByTenantAndState(tenantId, stateId);
    }

    @QueryMapping
    public List<City> citiesByCountry(@Argument Long countryId) {
        return cityService.getCitiesByCountry(countryId);
    }

    @QueryMapping
    public List<City> citiesByTenantAndCountry(@Argument String tenantId, @Argument Long countryId) {
        return cityService.getCitiesByTenantAndCountry(tenantId, countryId);
    }

    @QueryMapping
    public List<City> searchCities(@Argument String tenantId, @Argument String searchTerm) {
        return cityService.searchCities(tenantId, searchTerm);
    }

    @MutationMapping
    public City createCity(@Argument CityInput input) {
        CityRequest request = mapToRequest(input);
        return cityService.createCity(request);
    }

    @MutationMapping
    public City updateCity(@Argument Long id, @Argument CityInput input) {
        CityRequest request = mapToRequest(input);
        return cityService.updateCity(id, request);
    }

    @MutationMapping
    public Boolean deleteCity(@Argument Long id) {
        cityService.deleteCity(id);
        return true;
    }

    private CityRequest mapToRequest(CityInput input) {
        return CityRequest.builder()
                .tenantId(input.getTenantId())
                .countryId(input.getCountryId())
                .stateId(input.getStateId())
                .name(input.getName())
                .code(input.getCode())
                .pincode(input.getPincode())
                .latitude(input.getLatitude())
                .longitude(input.getLongitude())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy())
                .updatedBy(input.getUpdatedBy())
                .build();
    }
}
