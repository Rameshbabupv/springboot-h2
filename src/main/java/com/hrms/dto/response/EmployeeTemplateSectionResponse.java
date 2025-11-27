package com.hrms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTemplateSectionResponse {

    private Long id;
    private Long templateId;
    private String sectionName;
    private String sectionCode;
    private String sectionDescription;
    private Integer sectionOrder;
    private String sectionIcon;
    private Boolean isCollapsible;
    private Boolean isExpandedByDefault;
    private Map<String, Object> conditionalLogic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<EmployeeTemplateFieldResponse> fields = new ArrayList<>();
}
