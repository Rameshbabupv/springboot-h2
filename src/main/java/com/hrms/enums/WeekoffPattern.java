package com.hrms.enums;

/**
 * Week-off patterns for Indian workforce.
 * Defines which weeks of the month have off for a specific day.
 */
public enum WeekoffPattern {
    EVERY_WEEK,
    FIRST_ONLY,
    SECOND_ONLY,
    THIRD_ONLY,
    FOURTH_ONLY,
    FIFTH_ONLY,
    FIRST_THIRD,
    SECOND_FOURTH,
    FIRST_THIRD_FIFTH,
    SECOND_FOURTH_FIFTH,
    ALL_EXCEPT_FIRST,
    ALL_EXCEPT_FIFTH
}
