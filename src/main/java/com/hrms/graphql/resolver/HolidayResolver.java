package com.hrms.graphql.resolver;

import com.hrms.entity.Holiday;
import com.hrms.entity.HolidayCompanyMapping;
import com.hrms.entity.HolidayLocationMapping;
import com.hrms.graphql.input.HolidayInput;
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

    public HolidayResolver(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    // Queries

    @QueryMapping
    public List<Holiday> holidays(@Argument String tenantId,
                                  @Argument Long companyId,
                                  @Argument Integer year,
                                  @Argument Long locationId,
                                  @Argument String searchQuery) {
        return holidayService.getHolidays(tenantId, companyId, year, locationId, searchQuery);
    }

    @QueryMapping
    public Holiday holiday(@Argument String tenantId, @Argument Long id) {
        return holidayService.getHoliday(tenantId, id).orElse(null);
    }

    // Mutations

    @MutationMapping
    public Holiday createHoliday(@Argument String tenantId,
                                 @Argument HolidayInput input) {
        return holidayService.createHoliday(tenantId, input);
    }

    @MutationMapping
    public Holiday updateHoliday(@Argument String tenantId,
                                 @Argument Long id,
                                 @Argument HolidayInput input) {
        return holidayService.updateHoliday(tenantId, id, input);
    }

    @MutationMapping
    public Boolean deleteHoliday(@Argument String tenantId, @Argument Long id) {
        return holidayService.deleteHoliday(tenantId, id);
    }

    @MutationMapping
    public List<Holiday> bulkCreateHolidays(@Argument String tenantId,
                                            @Argument List<HolidayInput> inputs) {
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
