package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private Boolean isDefault;
    private Boolean isActive;
    private Integer priority;
    private String version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<EmployeeTemplateSectionResponse> sections = new ArrayList<>();
}
