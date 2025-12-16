package com.hrms.graphql.resolver;

import com.hrms.dto.request.CountryRequest;
import com.hrms.entity.Country;
import com.hrms.graphql.input.CountryInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.CountryService;
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
public class CountryResolver {

    private final CountryService countryService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    @QueryMapping
    public List<Country> countries() {
        return countryService.getAllCountries();
    }

    @QueryMapping
    public Country country(@Argument Long id) {
        return countryService.getCountryById(id);
    }

    @QueryMapping
    public List<Country> countriesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return countryService.getCountriesByTenant(tenantId);
    }

    @QueryMapping
    public List<Country> activeCountriesByTenant(@Argument(name = "tenantId") String tenantIdArg) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return countryService.getActiveCountriesByTenant(tenantId);
    }

    @QueryMapping
    public List<Country> activeCountries() {
        return countryService.getActiveCountries();
    }

    @QueryMapping
    public List<Country> searchCountries(@Argument(name = "tenantId") String tenantIdArg, @Argument String searchTerm) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return countryService.searchCountries(tenantId, searchTerm);
    }

    @MutationMapping
    public Country createCountry(@Argument CountryInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        CountryRequest request = mapToRequest(input, tenantId);
        return countryService.createCountry(request);
    }

    @MutationMapping
    public Country updateCountry(@Argument Long id, @Argument CountryInput input) {
        String tenantId = input.getTenantId() != null
            ? input.getTenantId()
            : jwtClaimsExtractor.getTenantIdOrFallback(null);
        CountryRequest request = mapToRequest(input, tenantId);
        return countryService.updateCountry(id, request);
    }

    @MutationMapping
    public Boolean deleteCountry(@Argument Long id) {
        countryService.deleteCountry(id);
        return true;
    }

    private CountryRequest mapToRequest(CountryInput input, String tenantId) {
        Long userId = jwtClaimsExtractor.getUserIdOrNull();
        String userIdStr = userId != null ? userId.toString() : null;

        return CountryRequest.builder()
                .tenantId(tenantId)
                .name(input.getName())
                .code(input.getCode())
                .currencyCode(input.getCurrencyCode())
                .phoneCode(input.getPhoneCode())
                .description(input.getDescription())
                .isActive(input.getIsActive())
                .createdBy(input.getCreatedBy() != null ? input.getCreatedBy() : userIdStr)
                .updatedBy(input.getUpdatedBy() != null ? input.getUpdatedBy() : userIdStr)
                .build();
    }
}
