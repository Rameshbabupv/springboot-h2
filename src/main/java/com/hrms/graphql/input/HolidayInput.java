package com.hrms.graphql.input;

import com.hrms.enums.HolidayCategory;
import com.hrms.enums.Religion;
import lombok.Data;

import java.util.List;

/**
 * GraphQL input for creating/updating Holiday.
 */
@Data
public class HolidayInput {
    private String date;
    private String name;
    private String holidayType;
    private HolidayCategory category;
    private Boolean isMandatory;
    private Boolean isNiActCompliant;
    private Boolean isFloating;
    private Religion religion;
    private String description;
    private List<Long> companyIds;
    private List<Long> locationIds;
}
