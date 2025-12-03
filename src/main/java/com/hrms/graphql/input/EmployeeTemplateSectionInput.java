package com.hrms.graphql.input;

import lombok.Data;

@Data
public class EmployeeTemplateSectionInput {
    private String templateId;
    private String sectionName;
    private String sectionCode;
    private String sectionDescription;
    private Integer sectionOrder;
    private String sectionIcon;
    private Boolean isCollapsible = true;
    private Boolean isExpandedByDefault = true;
    private String conditionalLogic;
}
