package com.hrms.graphql.resolver;

import com.hrms.entity.Holiday;
import com.hrms.entity.HolidayCompanyMapping;
import com.hrms.entity.HolidayLocationMapping;
import com.hrms.graphql.input.HolidayInput;
import com.hrms.security.JwtClaimsExtractor;
import com.hrms.service.HolidayService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphQL resolver for Holiday operations.
 */
@Controller
public class HolidayResolver {

    private final HolidayService holidayService;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public HolidayResolver(HolidayService holidayService, JwtClaimsExtractor jwtClaimsExtractor) {
        this.holidayService = holidayService;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    // Queries

    @QueryMapping
    public List<Holiday> holidays(@Argument(name = "tenantId") String tenantIdArg,
                                  @Argument Long companyId,
                                  @Argument Integer year,
                                  @Argument Long locationId,
                                  @Argument String searchQuery) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return holidayService.getHolidays(tenantId, companyId, year, locationId, searchQuery);
    }

    @QueryMapping
    public Holiday holiday(@Argument(name = "tenantId") String tenantIdArg, @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return holidayService.getHoliday(tenantId, id).orElse(null);
    }

    // Mutations

    @MutationMapping
    public Holiday createHoliday(@Argument(name = "tenantId") String tenantIdArg,
                                 @Argument HolidayInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return holidayService.createHoliday(tenantId, input);
    }

    @MutationMapping
    public Holiday updateHoliday(@Argument(name = "tenantId") String tenantIdArg,
                                 @Argument Long id,
                                 @Argument HolidayInput input) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return holidayService.updateHoliday(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteHoliday(@Argument(name = "tenantId") String tenantIdArg, @Argument Long id) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return holidayService.deleteHoliday(tenantId, id);
    }

    @MutationMapping
    public List<Holiday> bulkCreateHolidays(@Argument(name = "tenantId") String tenantIdArg,
                                            @Argument List<HolidayInput> inputs) {
        String tenantId = jwtClaimsExtractor.getTenantIdOrFallback(tenantIdArg);
        return holidayService.bulkCreateHolidays(tenantId, inputs);
    }

    // Field Resolvers

    /**
     * Resolve companies field from companyMappings.
     * Converts List<HolidayCompanyMapping> to List<Long> (company IDs).
     */
    @SchemaMapping(typeName = "Holiday", field = "companies")
    public List<Long> companies(Holiday holiday) {
        if (holiday.getCompanyMappings() == null || holiday.getCompanyMappings().isEmpty()) {
            return null;
        }
        return holiday.getCompanyMappings().stream()
                .map(mapping -> mapping.getCompany().getId())
                .collect(Collectors.toList());
    }

    /**
     * Resolve locations field from locationMappings.
     * Converts List<HolidayLocationMapping> to List<Long> (location IDs).
     */
    @SchemaMapping(typeName = "Holiday", field = "locations")
    public List<Long> locations(Holiday holiday) {
        if (holiday.getLocationMappings() == null || holiday.getLocationMappings().isEmpty()) {
            return null;
        }
        return holiday.getLocationMappings().stream()
                .map(mapping -> mapping.getLocation().getId())
                .collect(Collectors.toList());
    }
}
