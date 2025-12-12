package com.hrms.graphql.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * GraphQL input for creating/updating AttendancePolicyTemplate.
 */
@Data
public class AttendancePolicyTemplateInput {
    private String templateCode;
    private String templateName;
    private String description;
    private String changeNotes;
    private Boolean isDefault;
    private Boolean isActive;
    private Integer priority;
    private String effectiveFrom;
    private String effectiveTo;
    private Map<String, Object> criteria;
    private List<Long> applicableShiftIds;
    private List<WeekoffRuleInput> weekoffRules;
    private List<PolicyIncentiveInput> incentives;
    private Boolean treatAbsenceAsLeave;
    private Boolean markHolidays;
    private Boolean markWeekoffs;
    private Double minHoursForPresent;
    private Double minHoursForHalfDay;
    private Integer graceInMinutes;
    private Integer graceOutMinutes;
    private String firstHalfEnd;
    private String secondHalfStart;
    private Boolean isOTEligible;
    private Boolean hasIncentives;
}
