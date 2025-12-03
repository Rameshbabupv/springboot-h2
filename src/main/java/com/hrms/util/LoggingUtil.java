package com.hrms.util;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Utility class for formatting GraphQL response data in logs
 * Shows first 10 rows with key fields, truncates if more data exists
 */
public class LoggingUtil {

    private static final int MAX_ROWS_TO_LOG = 10;
    private static final List<String> KEY_FIELD_NAMES = Arrays.asList(
            "id", "empId", "username", "email", "name", "employeeName",
            "designation", "status", "companyId", "departmentId",
            "code", "title", "description", "firstName", "lastName"
    );

    /**
     * Format list of objects for logging (first 10 rows with key fields)
     * Example output: "3 items: [id=1, name=John], [id=2, name=Jane], ...and 1 more"
     */
    public static String formatListForLog(List<?> items) {
        if (items == null || items.isEmpty()) {
            return "0 items";
        }

        int totalSize = items.size();
        int displaySize = Math.min(MAX_ROWS_TO_LOG, totalSize);

        StringBuilder sb = new StringBuilder();
        sb.append(totalSize).append(" item").append(totalSize != 1 ? "s" : "");

        List<String> rowDetails = new ArrayList<>();
        for (int i = 0; i < displaySize; i++) {
            String row = formatObjectForLog(items.get(i));
            rowDetails.add(row);
        }

        for (String row : rowDetails) {
            sb.append("\n  - ").append(row);
        }

        if (totalSize > MAX_ROWS_TO_LOG) {
            sb.append("\n  ... and ").append(totalSize - MAX_ROWS_TO_LOG).append(" more");
        }

        return sb.toString();
    }

    /**
     * Format single object by extracting key fields
     * Example: "id=1, empId=EMP001, name=John Doe, designation=Manager"
     */
    public static String formatObjectForLog(Object obj) {
        if (obj == null) {
            return "null";
        }

        List<String> fieldParts = new ArrayList<>();
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();

            // Only include key fields
            if (!KEY_FIELD_NAMES.contains(fieldName)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (value != null) {
                    // Truncate long strings
                    String displayValue = value.toString();
                    if (displayValue.length() > 50) {
                        displayValue = displayValue.substring(0, 47) + "...";
                    }
                    fieldParts.add(fieldName + "=" + displayValue);
                }
            } catch (IllegalAccessException e) {
                // Skip fields that can't be accessed
            }
        }

        return String.join(", ", fieldParts);
    }

    /**
     * Format for paginated responses
     * Example: "11 items (page 1/1)"
     */
    public static String formatPaginatedListForLog(List<?> items, int currentPage, int totalPages, long totalElements) {
        if (items == null || items.isEmpty()) {
            return "0 items (page " + (currentPage + 1) + "/" + totalPages + ")";
        }

        int displaySize = Math.min(MAX_ROWS_TO_LOG, items.size());

        StringBuilder sb = new StringBuilder();
        sb.append(totalElements).append(" item").append(totalElements != 1 ? "s" : "")
          .append(" (page ").append(currentPage + 1).append("/").append(totalPages).append(")");

        List<String> rowDetails = new ArrayList<>();
        for (int i = 0; i < displaySize; i++) {
            String row = formatObjectForLog(items.get(i));
            rowDetails.add(row);
        }

        for (String row : rowDetails) {
            sb.append("\n  - ").append(row);
        }

        if (items.size() > MAX_ROWS_TO_LOG) {
            sb.append("\n  ... and ").append(items.size() - MAX_ROWS_TO_LOG).append(" more on this page");
        }

        return sb.toString();
    }
}
