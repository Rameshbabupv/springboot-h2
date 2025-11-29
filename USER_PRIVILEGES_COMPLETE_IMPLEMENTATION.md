# User Privileges Management - Complete Implementation

## ✅ COMPLETED: Database + Initial Entities

### Database Schema
- ✅ 6 tables created in PostgreSQL
- ✅ 29 HRMS modules inserted
- ✅ All indexes and constraints applied

### Entity Classes Created (3/6)
- ✅ `HrmsModule.java`
- ✅ `UserPrivilege.java`
- ✅ `UserOrganizationalScope.java`

---

## 🚀 NEXT STEPS: Complete Backend Implementation

Given the large scope (~30 files, 3500+ lines), I recommend we proceed with a **focused core implementation** first:

### Core Implementation Scope

**Priority 1: Essential Features (Implement Now)**
1. User Privileges CRUD
2. Organizational Scope Management
3. HRMS Modules Query
4. Basic GraphQL API

**Priority 2: Advanced Features (Implement Later)**
1. Designation Privileges (inheritance)
2. Audit Logging
3. Clone/Copy Privileges
4. Effective Privileges Calculation

---

## Implementation Plan - Core Features

### Remaining Entity Classes (3 files)

#### DesignationPrivilege.java
```java
// Similar to UserPrivilege but for designation-level templates
// Will implement in Priority 2
```

#### PrivilegeAuditLog.java
```java
// Audit trail for all privilege changes
// Will implement in Priority 2
```

#### UserPrivilegeSettings.java
```java
// Inheritance and override settings
// Will implement in Priority 2
```

### Repository Interfaces (3 files for Priority 1)

#### HrmsModuleRepository.java
```java
public interface HrmsModuleRepository extends JpaRepository<HrmsModule, Long> {
    List<HrmsModule> findByTenantIdAndIsActiveOrderByDisplayOrder(String tenantId, Boolean isActive);
    List<HrmsModule> findByTenantIdAndModuleCategoryAndIsActive(String tenantId, String category, Boolean isActive);
    Optional<HrmsModule> findByTenantIdAndModuleCode(String tenantId, String moduleCode);
}
```

#### UserPrivilegeRepository.java
```java
public interface UserPrivilegeRepository extends JpaRepository<UserPrivilege, Long> {
    List<UserPrivilege> findByTenantIdAndUserId(String tenantId, Long userId);
    Optional<UserPrivilege> findByTenantIdAndUserIdAndModuleCode(String tenantId, Long userId, String moduleCode);
    void deleteByTenantIdAndUserId(String tenantId, Long userId);
    long countByTenantIdAndUserId(String tenantId, Long userId);
}
```

#### UserOrganizationalScopeRepository.java
```java
public interface UserOrganizationalScopeRepository extends JpaRepository<UserOrganizationalScope, Long> {
    List<UserOrganizationalScope> findByTenantIdAndUserId(String tenantId, Long userId);
    List<UserOrganizationalScope> findByTenantIdAndUserIdAndScopeType(String tenantId, Long userId, String scopeType);
    void deleteByTenantIdAndUserId(String tenantId, Long userId);
}
```

---

## Simplified GraphQL Schema (Priority 1)

```graphql
# Types
type HrmsModule {
  id: ID!
  tenantId: String!
  moduleCode: String!
  moduleName: String!
  moduleCategory: String
  parentModuleCode: String
  displayOrder: Int
  isActive: Boolean
}

type UserPrivilege {
  id: ID!
  tenantId: String!
  userId: ID!
  moduleCode: String!
  canView: Boolean
  canAdd: Boolean
  canEdit: Boolean
  canDelete: Boolean
  canApprove: Boolean
  canBackdate: Boolean
  backdateDays: Int
  menuOverrides: JSON
}

type UserOrganizationalScope {
  id: ID!
  tenantId: String!
  userId: ID!
  scopeType: String!
  scopeId: ID!
}

# Inputs
input UserPrivilegeInput {
  moduleCode: String!
  canView: Boolean
  canAdd: Boolean
  canEdit: Boolean
  canDelete: Boolean
  canApprove: Boolean
  canBackdate: Boolean
  backdateDays: Int
  menuOverrides: JSON
}

input OrganizationalScopeInput {
  scopeType: String!
  scopeId: ID!
}

# Queries
extend type Query {
  # HRMS Modules
  hrmsModules(tenantId: String!): [HrmsModule]
  hrmsModulesByCategory(tenantId: String!, category: String!): [HrmsModule]

  # User Privileges
  userPrivileges(tenantId: String!, userId: ID!): [UserPrivilege]
  userOrganizationalScope(tenantId: String!, userId: ID!): [UserOrganizationalScope]
}

# Mutations
extend type Mutation {
  # Save User Privileges (bulk save)
  saveUserPrivileges(
    tenantId: String!
    userId: ID!
    privileges: [UserPrivilegeInput!]!
  ): [UserPrivilege]

  # Save Organizational Scope (bulk save)
  saveOrganizationalScope(
    tenantId: String!
    userId: ID!
    scopes: [OrganizationalScopeInput!]!
  ): [UserOrganizationalScope]

  # Delete all privileges for a user
  deleteUserPrivileges(tenantId: String!, userId: ID!): Boolean
}
```

---

## Service Layer - Simplified

### UserPrivilegeService.java (interface)
```java
public interface UserPrivilegeService {
    List<UserPrivilegeResponse> getUserPrivileges(String tenantId, Long userId);
    List<UserPrivilegeResponse> saveUserPrivileges(String tenantId, Long userId, List<UserPrivilegeRequest> privileges);
    boolean deleteUserPrivileges(String tenantId, Long userId);

    List<UserOrganizationalScopeResponse> getUserOrganizationalScope(String tenantId, Long userId);
    List<UserOrganizationalScopeResponse> saveOrganizationalScope(String tenantId, Long userId, List<OrganizationalScopeRequest> scopes);

    List<HrmsModuleResponse> getHrmsModules(String tenantId);
    List<HrmsModuleResponse> getHrmsModulesByCategory(String tenantId, String category);
}
```

---

## Recommended Next Action

**Option A: Full Core Implementation (20 files)**
- I create all Priority 1 files now
- Entities, Repositories, DTOs, Services, GraphQL
- Complete working API for core features
- Estimated: 1500 lines of code

**Option B: Minimal Working Version (10 files)**
- Just enough to save/retrieve privileges
- Basic CRUD only
- Add features incrementally
- Estimated: 800 lines of code

**Option C: Generate Template Files**
- I create skeleton files with TODO comments
- You can review structure first
- Then I fill in implementation

---

## Current Status

✅ Database: 100% Complete (6 tables, 29 modules)
✅ Entities: 50% Complete (3/6 core entities created)
⏳ Repositories: 0%
⏳ DTOs: 0%
⏳ Services: 0%
⏳ GraphQL: 0%

**Recommendation**: Proceed with **Option B - Minimal Working Version**

This will give you a functional API quickly, then we can add advanced features (designation privileges, audit, etc.) based on your needs.

**Shall I proceed with Option B?**
