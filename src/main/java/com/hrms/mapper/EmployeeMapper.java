package com.hrms.mapper;

import com.hrms.dto.request.EmployeeRequest;
import com.hrms.dto.response.EmployeeResponse;
import com.hrms.entity.Employee;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * MapStruct Mapper for Employee entity and DTOs
 * Automatically generates implementation for all 78 fields
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmployeeMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // =====================================================
    // REQUEST TO ENTITY MAPPING
    // =====================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true) // Set manually in service
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "jobFunction", ignore = true)
    @Mapping(target = "employmentType", ignore = true)
    @Mapping(target = "division", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "reportingManager", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dateOfJoin", source = "dateOfJoin", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dateOfConfirm", source = "dateOfConfirm", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dateOfRetirement", source = "dateOfRetirement", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "effectFromSalary", source = "effectFromSalary", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "pfEnrollmentDate", source = "pfEnrollmentDate", qualifiedByName = "stringToLocalDate")
    Employee toEntity(EmployeeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "jobFunction", ignore = true)
    @Mapping(target = "employmentType", ignore = true)
    @Mapping(target = "division", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "reportingManager", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dateOfJoin", source = "dateOfJoin", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dateOfConfirm", source = "dateOfConfirm", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "dateOfRetirement", source = "dateOfRetirement", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "effectFromSalary", source = "effectFromSalary", qualifiedByName = "stringToLocalDate")
    @Mapping(target = "pfEnrollmentDate", source = "pfEnrollmentDate", qualifiedByName = "stringToLocalDate")
    void updateEntityFromRequest(EmployeeRequest request, @MappingTarget Employee employee);

    // =====================================================
    // ENTITY TO RESPONSE MAPPING
    // =====================================================

    EmployeeResponse toResponse(Employee employee);

    // =====================================================
    // CUSTOM MAPPING METHODS
    // =====================================================

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
