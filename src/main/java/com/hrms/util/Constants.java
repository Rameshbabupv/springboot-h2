package com.hrms.util;

/**
 * Application-wide constants.
 */
public final class Constants {

    private Constants() {
        // Utility class - prevent instantiation
    }

    // API Paths
    public static final String API_BASE_PATH = "/api";
    public static final String API_V1_PATH = "/api/v1";

    // Entity Names
    public static final String COMPANY = "Company";
    public static final String EMPLOYEE = "Employee";
    public static final String DEPARTMENT = "Department";
    public static final String DESIGNATION = "Designation";
    public static final String STATE = "State";
    public static final String CITY = "City";
    public static final String DIVISION = "Division";
    public static final String SECTION = "Section";
    public static final String GRADE = "Grade";
    public static final String JOB_FUNCTION = "JobFunction";
    public static final String EMPLOYMENT_TYPE = "EmploymentType";

    // Common Field Names
    public static final String FIELD_ID = "id";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_TENANT_ID = "tenantId";

    // Employee Status Values
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_ON_LEAVE = "ON_LEAVE";
    public static final String STATUS_RESIGNED = "RESIGNED";
    public static final String STATUS_TERMINATED = "TERMINATED";

    // Gender Values
    public static final String GENDER_MALE = "Male";
    public static final String GENDER_FEMALE = "Female";
    public static final String GENDER_OTHER = "Other";

    // Marital Status Values
    public static final String MARITAL_SINGLE = "Single";
    public static final String MARITAL_MARRIED = "Married";
    public static final String MARITAL_DIVORCED = "Divorced";
    public static final String MARITAL_WIDOWED = "Widowed";

    // Blood Group Values
    public static final String BLOOD_A_POSITIVE = "A+";
    public static final String BLOOD_A_NEGATIVE = "A-";
    public static final String BLOOD_B_POSITIVE = "B+";
    public static final String BLOOD_B_NEGATIVE = "B-";
    public static final String BLOOD_AB_POSITIVE = "AB+";
    public static final String BLOOD_AB_NEGATIVE = "AB-";
    public static final String BLOOD_O_POSITIVE = "O+";
    public static final String BLOOD_O_NEGATIVE = "O-";

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Pagination Defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // Success Messages
    public static final String MSG_CREATED = "%s created successfully";
    public static final String MSG_UPDATED = "%s updated successfully";
    public static final String MSG_DELETED = "%s deleted successfully";
    public static final String MSG_FETCHED = "%s fetched successfully";

    // Error Messages
    public static final String MSG_NOT_FOUND = "%s not found with %s: '%s'";
    public static final String MSG_DUPLICATE = "%s already exists with %s: '%s'";
    public static final String MSG_VALIDATION_FAILED = "Validation failed";
}
