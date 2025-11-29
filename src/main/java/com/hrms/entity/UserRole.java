package com.hrms.entity;

/**
 * UserRole Enum - Predefined user roles for RBAC
 *
 * Role Hierarchy:
 * - SYSTEM_ADMIN: Full system access (highest privilege)
 * - COMPANY_ADMIN: Company-wide administrative access
 * - HR_MANAGER: Complete HR operations
 * - PAYROLL_ADMIN: Payroll processing
 * - DEPARTMENT_MANAGER: Department-level management
 * - ACCOUNTANT: Financial operations
 * - RECRUITER: Recruitment operations
 * - MANAGER: General manager role
 * - EMPLOYEE_SELF_SERVICE: Employee portal access
 * - PORTAL: Limited portal access
 * - CUSTOM: Custom configurable role
 */
public enum UserRole {

    SYSTEM_ADMIN("System Admin", "Full system access with all privileges"),
    COMPANY_ADMIN("Company Admin", "Company-wide administrative access"),
    HR_MANAGER("HR Manager", "Complete HR operations and employee management"),
    PAYROLL_ADMIN("Payroll Admin", "Payroll processing and salary management"),
    DEPARTMENT_MANAGER("Department Manager", "Team management and approvals for department"),
    ACCOUNTANT("Accountant", "Financial operations and reporting"),
    RECRUITER("Recruiter", "Recruitment and onboarding management"),
    EMPLOYEE_SELF_SERVICE("Employee Self-Service", "Self-service portal for employees"),
    MANAGER("Manager", "General manager role with approval rights"),
    PORTAL("Portal User", "Limited access to employee portal"),
    CUSTOM("Custom Role", "Configure permissions manually");

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
     * Get role from string value
     */
    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Invalid user role: " + role);
    }

    /**
     * Check if role is valid
     */
    public static boolean isValid(String role) {
        try {
            fromString(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
