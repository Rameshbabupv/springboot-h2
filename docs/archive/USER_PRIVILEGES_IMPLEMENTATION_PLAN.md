# User Privileges Management - Implementation Plan

## Status: IN PROGRESS

### Phase 1: Database Schema ✅ COMPLETED
**File**: `sql/07_user_privileges_tables.sql`

**Tables Created**:
1. ✅ `hrms_modules` - Master table of all HRMS modules (29 sample modules inserted)
2. ✅ `user_privileges` - Module-level permissions for users
3. ✅ `user_organizational_scope` - Organizational boundaries (Company, Location, etc.)
4. ✅ `designation_privileges` - Template permissions at designation level
5. ✅ `privilege_audit_log` - Complete audit trail
6. ✅ `user_privilege_settings` - Inheritance and override settings

**Features**:
- Multi-tenant support
- Granular permissions (View, Add, Edit, Delete, Approve, Backdate)
- JSONB for menu overrides
- 9 organizational scope types
- Complete audit trail
- Foreign key constraints to user_account table

---

### Phase 2: Backend Implementation (IN PROGRESS)

#### 2.1 Entity Classes (6 files needed)
Location: `src/main/java/com/hrms/entity/`

1. **HrmsModule.java** - HRMS module master entity
2. **UserPrivilege.java** - User privilege entity
3. **UserOrganizationalScope.java** - Organizational scope entity
4. **DesignationPrivilege.java** - Designation privilege template
5. **PrivilegeAuditLog.java** - Audit log entity
6. **UserPrivilegeSettings.java** - User settings entity

#### 2.2 Repository Interfaces (6 files needed)
Location: `src/main/java/com/hrms/repository/`

1. **HrmsModuleRepository.java**
2. **UserPrivilegeRepository.java**
3. **UserOrganizationalScopeRepository.java**
4. **DesignationPrivilegeRepository.java**
5. **PrivilegeAuditLogRepository.java**
6. **UserPrivilegeSettingsRepository.java**

#### 2.3 DTOs (12 files needed)
Location: `src/main/java/com/hrms/dto/`

**Request DTOs**:
- `UserPrivilegeRequest.java`
- `UserPrivilegeBulkRequest.java`
- `OrganizationalScopeRequest.java`
- `DesignationPrivilegeRequest.java`

**Response DTOs**:
- `HrmsModuleResponse.java`
- `UserPrivilegeResponse.java`
- `UserOrganizationalScopeResponse.java`
- `DesignationPrivilegeResponse.java`
- `PrivilegeAuditLogResponse.java`
- `UserPrivilegeSettingsResponse.java`
- `UserEffectivePrivilegesResponse.java` (combined view)

**GraphQL Input Types**:
- `UserPrivilegeInput.java`
- `OrganizationalScopeInput.java`

#### 2.4 Service Layer (4 files needed)
Location: `src/main/java/com/hrms/service/`

**Interfaces**:
- `UserPrivilegeService.java`
- `DesignationPrivilegeService.java`

**Implementations**:
- `UserPrivilegeServiceImpl.java`
- `DesignationPrivilegeServiceImpl.java`

**Key Service Methods**:
```java
// User Privileges
List<UserPrivilegeResponse> getUserPrivileges(String tenantId, Long userId);
UserEffectivePrivilegesResponse getEffectivePrivileges(String tenantId, Long userId);
List<UserPrivilegeResponse> saveUserPrivileges(String tenantId, Long userId, List<UserPrivilegeRequest> privileges);
List<UserPrivilegeResponse> cloneUserPrivileges(String tenantId, Long fromUserId, Long toUserId);

// Organizational Scope
List<UserOrganizationalScopeResponse> getUserOrganizationalScope(String tenantId, Long userId);
List<UserOrganizationalScopeResponse> saveOrganizationalScope(String tenantId, Long userId, List<OrganizationalScopeRequest> scopes);

// Designation Privileges
List<DesignationPrivilegeResponse> getDesignationPrivileges(String tenantId, Long designationId);
List<DesignationPrivilegeResponse> saveDesignationPrivileges(String tenantId, Long designationId, List<DesignationPrivilegeRequest> privileges);

// Audit
List<PrivilegeAuditLogResponse> getPrivilegeAuditLog(String tenantId, Long userId, Integer limit);
```

#### 2.5 Mapper (1 file needed)
Location: `src/main/java/com/hrms/mapper/`

- `UserPrivilegeMapper.java` (MapStruct)

#### 2.6 GraphQL Schema (1 file needed)
Location: `src/main/resources/graphql/`

- `user-privileges.graphqls`

**GraphQL Operations**:

**Queries**:
```graphql
type Query {
  # Module Master
  hrmsModules(tenantId: String!): [HrmsModule]
  hrmsModulesByCategory(tenantId: String!, category: String!): [HrmsModule]

  # User Privileges
  userPrivileges(tenantId: String!, userId: ID!): [UserPrivilege]
  userEffectivePrivileges(tenantId: String!, userId: ID!): UserEffectivePrivileges
  userOrganizationalScope(tenantId: String!, userId: ID!): [UserOrganizationalScope]

  # Designation Privileges
  designationPrivileges(tenantId: String!, designationId: ID!): [DesignationPrivilege]

  # Audit
  privilegeAuditLog(tenantId: String!, userId: ID, limit: Int): [PrivilegeAuditLog]
}
```

**Mutations**:
```graphql
type Mutation {
  # Save User Privileges
  saveUserPrivileges(tenantId: String!, userId: ID!, privileges: [UserPrivilegeInput!]!): [UserPrivilege]

  # Save Organizational Scope
  saveOrganizationalScope(tenantId: String!, userId: ID!, scopes: [OrganizationalScopeInput!]!): [UserOrganizationalScope]

  # Save Designation Privileges
  saveDesignationPrivileges(tenantId: String!, designationId: ID!, privileges: [UserPrivilegeInput!]!): [DesignationPrivilege]

  # Clone Privileges
  cloneUserPrivileges(tenantId: String!, fromUserId: ID!, toUserId: ID!): [UserPrivilege]

  # Delete Privileges
  deleteUserPrivileges(tenantId: String!, userId: ID!): Boolean
  deleteOrganizationalScope(tenantId: String!, userId: ID!): Boolean
}
```

#### 2.7 GraphQL Resolver (1 file needed)
Location: `src/main/java/com/hrms/graphql/resolver/`

- `UserPrivilegeResolver.java`

---

### Phase 3: Implementation Complexity

**Total Files to Create**: ~30 files
**Estimated Lines of Code**: ~3,500 lines
**Implementation Time**: 2-3 hours

**Key Challenges**:
1. Complex inheritance logic (user privileges + designation privileges)
2. Organizational scope filtering
3. Audit logging for all changes
4. Menu override JSONB handling
5. Effective privileges calculation

---

### Phase 4: Testing Plan

1. **Module Master**
   - Verify 29 modules inserted
   - Test module hierarchy

2. **User Privileges CRUD**
   - Create privileges for a user
   - Update privileges
   - Delete privileges
   - Query privileges

3. **Organizational Scope**
   - Save multiple scopes (Company, Location, Department)
   - Query scopes
   - Validate unique constraints

4. **Designation Privileges**
   - Create designation template
   - Test inheritance to users

5. **Effective Privileges**
   - User with designation inheritance
   - User with custom overrides
   - Verify combined permissions

6. **Audit Trail**
   - Verify all changes logged
   - Query audit history

---

### Recommended Approach

Given the size of this implementation, I recommend:

**Option A: Full Backend Implementation**
- I create all 30 files
- Complete backend API ready
- Takes significant time and tokens

**Option B: Incremental Implementation**
- Start with core entities and repositories
- Then services and GraphQL layer
- Test at each stage

**Option C: Essential First**
- Implement just user privileges (not designation)
- Get basic CRUD working
- Add advanced features later

**Which approach would you prefer?**

---

### Current Status

✅ Database schema created and migrated
✅ 29 HRMS modules inserted as sample data
✅ All indexes and constraints in place
⏳ Waiting for approval to proceed with backend implementation

**Next Step**: Create Entity classes (6 files)
