package com.hrms.graphql.input;

import com.hrms.enums.LeaveCategory;
import lombok.Data;

@Data
public class LeaveTypeInput {
    private String code;
    private String name;
    private LeaveCategory category;
    private String description;
    private String icon;
    private String color;
    private Boolean isActive;
}
