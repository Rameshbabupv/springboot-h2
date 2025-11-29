package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateResponse {

    private Long id;
    private String tenantId;
    private String templateName;
    private String templateCode;
    private String description;
    private String changeNotes;
    @Builder.Default
    private List<String> applicableCategories = new ArrayList<>();
    @Builder.Default
    private List<String> applicableGroups = new ArrayList<>();
    @Builder.Default
    private List<String> applicableGrades = new ArrayList<>();
    @Builder.Default
    private List<Long> applicableCompanies = new ArrayList<>();
    @Builder.Default
    private List<Long> applicableLocations = new ArrayList<>();
    @Builder.Default
    private List<String> applicableDivisions = new ArrayList<>();
    @Builder.Default
    private List<String> applicableDepartments = new ArrayList<>();
    @Builder.Default
    private List<String> applicableSections = new ArrayList<>();
    @Builder.Default
    private List<String> applicableDesignations = new ArrayList<>();
    @Builder.Default
    private List<String> applicableJobFunctions = new ArrayList<>();
    @Builder.Default
    private List<String> applicableEmploymentTypes = new ArrayList<>();

    // Field Configuration
    private String standardFields;
    private String customFields;

    private Boolean isDefault;
    private Boolean isActive;
    private Integer priority;
    private String version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private OffsetDateTime updatedAt;
    @Builder.Default
    private List<EmployeeTemplateSectionResponse> sections = new ArrayList<>();
}
