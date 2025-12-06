package com.hrms.graphql.input;

import com.hrms.enums.WeekDay;
import com.hrms.enums.WeekoffPattern;
import lombok.Data;

/**
 * GraphQL input for WeekoffRule.
 */
@Data
public class WeekoffRuleInput {
    private WeekDay dayOfWeek;
    private WeekoffPattern pattern;
}
