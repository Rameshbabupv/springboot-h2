package com.hrms.graphql.input;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class EmployeeTemplateInput {
    private String tenantId;
    private String templateName;
    private String templateCode;
    private String description;
    private String changeNotes;
    private List<String> applicableCategories = new ArrayList<>();
    private List<String> applicableGroups = new ArrayList<>();
    private List<String> applicableGrades = new ArrayList<>();
    private List<String> applicableCompanies = new ArrayList<>();
    private List<String> applicableLocations = new ArrayList<>();
    private List<String> applicableDivisions = new ArrayList<>();
    private List<String> applicableDepartments = new ArrayList<>();
    private List<String> applicableSections = new ArrayList<>();
    private List<String> applicableDesignations = new ArrayList<>();
    private List<String> applicableJobFunctions = new ArrayList<>();
    private List<String> applicableEmploymentTypes = new ArrayList<>();

    // Field Configuration
    private String standardFields;
    private String customFields;

    private Boolean isDefault = false;
    private Boolean isActive = true;
    private Integer priority = 0;
    private String version;
    private String effectiveFrom;
    private String effectiveTo;
    private String createdBy;
    private String updatedBy;
}
