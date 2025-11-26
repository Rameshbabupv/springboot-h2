package com.hrms.graphql.resolver;

import com.hrms.dto.request.CountryRequest;
import com.hrms.entity.Country;
import com.hrms.graphql.input.CountryInput;
import com.hrms.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CountryResolver {

    private final CountryService countryService;

    @QueryMapping
    public List<Country> countries() {
        return countryService.getAllCountries();
    }

    @QueryMapping
    public Country country(@Argument Long id) {
        return countryService.getCountryById(id);
    }

    @QueryMapping
    public List<Country> countriesByTenant(@Argument String tenantId) {
        return countryService.getCountriesByTenant(tenantId);
    }

    @QueryMapping
    public List<Country> activeCountriesByTenant(@Argument String tenantId) {
        return countryService.getActiveCountriesByTenant(tenantId);
    }

    @QueryMapping
    public List<Country> activeCountries() {
        return countryService.getActiveCountries();
    }

    @QueryMapping
    public List<Country> searchCountries(@Argument String tenantId, @Argument String searchTerm) {
        return countryService.searchCountries(tenantId, searchTerm);
    }

    @MutationMapping
    public Country createCountry(@Argument CountryInput input) {
        CountryRequest request = mapToRequest(input);
        return countryService.createCountry(request);
    }

    @MutationMapping
    public Country updateCountry(@Argument Long id, @Argument CountryInput input) {
        CountryRequest request = mapToRequest(input);
        return countryService.updateCountry(id, request);
    }

    @MutationMapping
    public Boolean deleteCountry(@Argument Long id) {
        countryService.deleteCountry(id);
        return true;
    }

    private CountryRequest mapToRequest(CountryInput input) {
        return CountryRequest.builder()
                .tenantId(input.getTenantId())
                .name(input.getName())
                .code(input.getCode())
                .currencyCode(input.getCurrencyCode())
                .phoneCode(input.getPhoneCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy())
                .updatedBy(input.getUpdatedBy())
                .build();
    }
}
