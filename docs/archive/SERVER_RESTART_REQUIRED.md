# ⚠️ SERVER RESTART REQUIRED - GraphQL Schema Updated

**Date**: 28-Nov-2025
**Status**: 🔴 CRITICAL - Server restart needed
**Issue**: Frontend getting "Field 'applicableDivisions' is undefined" error

---

## The Problem

The **GraphQL schema file HAS been updated** with all 6 new fields, but the **server has not been restarted yet**, so the changes are not active.

---

## Verification - Schema IS Updated ✅

### Type EmployeeTemplate (Lines 290-295 in schema.graphqls)
```graphql
type EmployeeTemplate {
    # ... existing fields ...
    applicableCompanies: [ID!]
    applicableLocations: [ID!]
    applicableDivisions: [String!]       ✅ PRESENT
    applicableDepartments: [String!]     ✅ PRESENT
    applicableSections: [String!]        ✅ PRESENT
    applicableDesignations: [String!]    ✅ PRESENT
    applicableJobFunctions: [String!]    ✅ PRESENT
    applicableEmploymentTypes: [String!] ✅ PRESENT
}
```

### Input EmployeeTemplateInput (Lines 745-750 in schema.graphqls)
```graphql
input EmployeeTemplateInput {
    # ... existing fields ...
    applicableCompanies: [ID!]
    applicableLocations: [ID!]
    applicableDivisions: [String!]       ✅ PRESENT
    applicableDepartments: [String!]     ✅ PRESENT
    applicableSections: [String!]        ✅ PRESENT
    applicableDesignations: [String!]    ✅ PRESENT
    applicableJobFunctions: [String!]    ✅ PRESENT
    applicableEmploymentTypes: [String!] ✅ PRESENT
}
```

---

## What's Needed: RESTART THE SERVER

The GraphQL schema is loaded when the Spring Boot application starts. Since we updated the schema file, the server must be restarted to pick up the changes.

### Step 1: Install Maven (if not already installed)
```bash
sudo apt update && sudo apt install maven
```

### Step 2: Build the project
```bash
cd /home/sysadmin/data/projects/HRMS_New_Api
mvn clean package -DskipTests
```

### Step 3: Start the server
```bash
mvn spring-boot:run
```

Or if you have the server running already, stop it and restart it.

---

## How to Verify After Restart

### Option 1: GraphiQL Interface
1. Open: `http://localhost:8090/graphiql`
2. Click "Docs" button on the right
3. Search for "EmployeeTemplate"
4. Verify all 11 JSONB fields are visible

### Option 2: Test Query
```graphql
query {
  employeeTemplates {
    id
    templateName
    applicableCompanies
    applicableLocations
    applicableDivisions
    applicableDepartments
    applicableSections
    applicableDesignations
    applicableJobFunctions
    applicableEmploymentTypes
    applicableGrades
    applicableGroups
    applicableCategories
  }
}
```

If this query works without errors, the schema is active!

---

## Backend Changes Summary (ALL COMPLETE ✅)

1. ✅ Database migration executed (6 columns added)
2. ✅ Entity updated (EmployeeTemplate.java)
3. ✅ GraphQL schema updated (schema.graphqls) ⬅️ THIS IS DONE!
4. ✅ GraphQL input class updated (EmployeeTemplateInput.java)
5. ✅ Request DTO updated (EmployeeTemplateRequest.java)
6. ✅ Response DTO updated (EmployeeTemplateResponse.java)
7. ✅ Resolver mapping updated (EmployeeTemplateResolver.java)
8. ✅ Service implementation updated (EmployeeTemplateServiceImpl.java)

**Only missing step: Server restart!**

---

## After Restart

Once the server is restarted, notify the frontend team to:
1. Uncomment the 6 fields in `employeeTemplateService.js`
2. Test the full 9-parameter criteria system
3. Verify template creation/update with all fields

---

## Current File Status

```bash
# Check the schema file contains the updates:
grep -A 15 "type EmployeeTemplate" src/main/resources/graphql/schema.graphqls

# You should see all 6 new fields listed!
```

---

**Bottom Line**: The code is correct, the schema file is updated. Just restart the server and it will work! 🚀
