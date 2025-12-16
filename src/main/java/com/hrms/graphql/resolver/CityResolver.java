package com.hrms.graphql.resolver;

import com.hrms.dto.request.CityRequest;
import com.hrms.entity.City;
import com.hrms.graphql.input.CityInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CityResolver {

    private final CityService cityService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<City> cities() {
        return cityService.getAllCities();
    }

    @QueryMapping
    public City city(@Argument Long id) {
        return cityService.getCityById(id);
    }

    @QueryMapping
    public List<City> citiesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return cityService.getCitiesByTenant(tenantId);
    }

    @QueryMapping
    public List<City> activeCitiesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
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
    public List<City> citiesByTenantAndState(@Argument(name = "tenantId") String tenantIdArg, @Argument Long stateId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return cityService.getCitiesByTenantAndState(tenantId, stateId);
    }

    @QueryMapping
    public List<City> citiesByCountry(@Argument Long countryId) {
        return cityService.getCitiesByCountry(countryId);
    }

    @QueryMapping
    public List<City> citiesByTenantAndCountry(@Argument(name = "tenantId") String tenantIdArg, @Argument Long countryId) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return cityService.getCitiesByTenantAndCountry(tenantId, countryId);
    }

    @QueryMapping
    public List<City> searchCities(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return cityService.searchCities(tenantId, searchTerm);
    }

    @MutationMapping
    public City createCity(@Argument CityInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        CityRequest request = mapToRequest(input, tenantId);
        return cityService.createCity(request);
    }

    @MutationMapping
    public City updateCity(@Argument Long id, @Argument CityInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        CityRequest request = mapToRequest(input, tenantId);
        return cityService.updateCity(id, request);
    }

    @MutationMapping
    public Boolean deleteCity(@Argument Long id) {
        cityService.deleteCity(id);
        return true;
    }

    private CityRequest mapToRequest(CityInput input, String tenantId) {
        Long userId = jwtClaimsExtractor.getUserIdOrNull();
        String userIdStr = userId != null ? userId.toString() : null;

        return CityRequest.builder()
                .tenantId(tenantId)
                .countryId(input.getCountryId())
                .stateId(input.getStateId())
                .name(input.getName())
                .code(input.getCode())
                .pincode(input.getPincode())
                .latitude(input.getLatitude())
                .longitude(input.getLongitude())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy() != null ? input.getCreatedBy() : userIdStr)
                .updatedBy(input.getUpdatedBy() != null ? input.getUpdatedBy() : userIdStr)
                .build();
    }
}
