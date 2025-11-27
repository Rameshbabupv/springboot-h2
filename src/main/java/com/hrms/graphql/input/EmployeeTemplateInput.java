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
    private List<String> applicableCategories = new ArrayList<>();
    private List<String> applicableGroups = new ArrayList<>();
    private List<String> applicableGrades = new ArrayList<>();
    private List<String> applicableCompanies = new ArrayList<>();
    private List<String> applicableLocations = new ArrayList<>();
    private Boolean isDefault = false;
    private Boolean isActive = true;
    private Integer priority = 0;
    private String version;
    private String effectiveFrom;
    private String effectiveTo;
    private String createdBy;
    private String updatedBy;
}
