package com.hrms.entity;

/**
 * UserRole Enum - Simplified 3-tier role system
 *
 * Role Hierarchy:
 * - ADMIN: Full access with 3 organizational filters (Company, Location, Department)
 * - MANAGER: Department/team level with 9 organizational filters (all org units)
 * - PORTAL: Self-service only (employee portal access)
 */
public enum UserRole {

    ADMIN("Admin", "Full administrative access with company-wide permissions"),
    MANAGER("Manager", "Department/team level management with approval rights"),
    PORTAL("Portal User", "Self-service employee portal access"),

    // Legacy roles - kept for backward compatibility, mapped to new roles
    @Deprecated SYSTEM_ADMIN("System Admin", "Legacy: maps to ADMIN"),
    @Deprecated COMPANY_ADMIN("Company Admin", "Legacy: maps to ADMIN"),
    @Deprecated HR_MANAGER("HR Manager", "Legacy: maps to MANAGER"),
    @Deprecated PAYROLL_ADMIN("Payroll Admin", "Legacy: maps to MANAGER"),
    @Deprecated DEPARTMENT_MANAGER("Department Manager", "Legacy: maps to MANAGER"),
    @Deprecated ACCOUNTANT("Accountant", "Legacy: maps to MANAGER"),
    @Deprecated RECRUITER("Recruiter", "Legacy: maps to MANAGER"),
    @Deprecated EMPLOYEE_SELF_SERVICE("Employee Self-Service", "Legacy: maps to PORTAL"),
    @Deprecated CUSTOM("Custom Role", "Legacy: maps to MANAGER");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this is a primary role (not legacy)
     */
    public boolean isPrimaryRole() {
        return this == ADMIN || this == MANAGER || this == PORTAL;
    }

    /**
     * Get the primary role equivalent for legacy roles
     */
    public UserRole toPrimaryRole() {
        switch (this) {
            case ADMIN:
                return ADMIN;
            case SYSTEM_ADMIN:
            case COMPANY_ADMIN:
                return ADMIN;
            case MANAGER:
            case HR_MANAGER:
            case PAYROLL_ADMIN:
            case DEPARTMENT_MANAGER:
            case ACCOUNTANT:
            case RECRUITER:
            case CUSTOM:
                return MANAGER;
            case PORTAL:
            case EMPLOYEE_SELF_SERVICE:
                return PORTAL;
            default:
                return MANAGER; // Default fallback
        }
    }

    /**
     * Get role from string value
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String normalized = role.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(normalized)) {
                return userRole;
            }
        }

        throw new IllegalArgumentException("Invalid user role: " + role);
    }

    /**
     * Check if role is valid
     */
    public static boolean isValid(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }

        // Handle legacy/common role mappings
        String normalizedRole = normalizeRole(role);

        try {
            fromString(normalizedRole);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Normalize role string to handle common variations
     * Maps all variations to the 3 primary roles: ADMIN, MANAGER, PORTAL
     */
    public static String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String trimmed = role.trim().toUpperCase().replace(" ", "_").replace("-", "_");

        // Direct match - primary roles
        if (trimmed.equals("ADMIN") || trimmed.equals("MANAGER") || trimmed.equals("PORTAL")) {
            return trimmed;
        }

        // Map legacy/common variations to primary roles
        switch (trimmed) {
            // Map to ADMIN
            case "SYSTEM_ADMIN":
            case "COMPANY_ADMIN":
            case "ADMINISTRATOR":
            case "SUPER_ADMIN":
            case "SUPERADMIN":
                return "ADMIN";

            // Map to MANAGER
            case "HR_MANAGER":
            case "PAYROLL_ADMIN":
            case "DEPARTMENT_MANAGER":
            case "ACCOUNTANT":
            case "RECRUITER":
            case "CUSTOM":
            case "TEAM_LEAD":
            case "SUPERVISOR":
                return "MANAGER";

            // Map to PORTAL
            case "EMPLOYEE_SELF_SERVICE":
            case "EMPLOYEE":
            case "PORTAL_USER":
            case "USER":
            case "SELF_SERVICE":
                return "PORTAL";

            default:
                // If no match, try to extract from the string
                if (trimmed.contains("ADMIN")) {
                    return "ADMIN";
                } else if (trimmed.contains("MANAGER") || trimmed.contains("LEAD")) {
                    return "MANAGER";
                } else if (trimmed.contains("PORTAL") || trimmed.contains("EMPLOYEE")) {
                    return "PORTAL";
                }
                return trimmed; // Return as-is if no match
        }
    }

    /**
     * Get all primary roles (for dropdown/selection)
     */
    public static UserRole[] getPrimaryRoles() {
        return new UserRole[]{ADMIN, MANAGER, PORTAL};
    }
}
