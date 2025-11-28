package com.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateRequest {

    @NotNull(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Template name is required")
    @Size(max = 200, message = "Template name must not exceed 200 characters")
    private String templateName;

    @NotBlank(message = "Template code is required")
    @Size(max = 100, message = "Template code must not exceed 100 characters")
    private String templateCode;

    private String description;

    private String changeNotes;

    private List<String> applicableCategories = new ArrayList<>();

    private List<String> applicableGroups = new ArrayList<>();

    private List<String> applicableGrades = new ArrayList<>();

    private List<Long> applicableCompanies = new ArrayList<>();

    private List<Long> applicableLocations = new ArrayList<>();

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

    @Size(max = 20, message = "Version must not exceed 20 characters")
    private String version;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Long createdBy;

    private Long updatedBy;
}
