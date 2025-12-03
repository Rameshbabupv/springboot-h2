# Attendance Module - Planning & Design
**Date**: 2025-Dec-03
**Branch**: `feature-attendance-module`
**Status**: 🔄 Planning Phase - Ready for Implementation
**Model**: Claude Haiku 4.5

---

## 📋 Overview

The Attendance Module is a new feature for the HRMS SaaS system that tracks employee attendance, working hours, shift timings, and generates attendance reports. This document outlines the complete design for database schema, API endpoints, and business logic.

---

## 🏗️ Architecture & Scope

### What Attendance Module Will Include
1. **Attendance Tracking** - Daily attendance records with check-in/check-out
2. **Shift Management** - Multiple shift timings per employee
3. **Attendance Status** - Present, Absent, Late, Half-day, Leave, etc.
4. **Working Hours Calculation** - Track daily and monthly working hours
5. **Reports** - Attendance summaries and compliance reports
6. **Multi-Tenant Support** - Isolated data per tenant

### What's Out of Scope (For Now)
- ❌ Biometric integration
- ❌ Geolocation tracking
- ❌ Approval workflows for leave
- ❌ Email notifications
- ❌ Mobile app integration

---

## 📊 Database Schema Design

### 1. ShiftTiming Entity
**Purpose**: Define shift schedules for employees

**Table**: `shift_timings`

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGINT | PK, Auto | Primary key |
| tenant_id | VARCHAR(50) | NOT NULL | Multi-tenancy |
| shift_code | VARCHAR(50) | NOT NULL | Unique shift identifier |
| shift_name | VARCHAR(100) | NOT NULL | Shift display name |
| start_time | TIME | NOT NULL | Shift start (e.g., 09:00) |
| end_time | TIME | NOT NULL | Shift end (e.g., 18:00) |
| working_hours | DECIMAL(4,2) | NOT NULL | Total hours (e.g., 8.5) |
| break_duration | INT | DEFAULT 60 | Break time in minutes |
| is_active | BOOLEAN | DEFAULT true | Active/Inactive |
| created_at | TIMESTAMP | Auto | Creation timestamp |
| updated_at | TIMESTAMP | Auto | Update timestamp |

**Indexes**:
- `idx_tenantid_active` on (tenant_id, is_active)
- `idx_shift_code` on (shift_code)

**Example Data**:
```
shift_code=MORNING, shift_name=Morning Shift, start_time=09:00, end_time=18:00, working_hours=8.5
shift_code=EVENING, shift_name=Evening Shift, start_time=14:00, end_time=23:00, working_hours=8.5
shift_code=NIGHT, shift_name=Night Shift, start_time=22:00, end_time=07:00, working_hours=8.5
shift_code=FLEXIBLE, shift_name=Flexible Hours, start_time=10:00, end_time=19:00, working_hours=8
```

---

### 2. AttendanceStatus Entity
**Purpose**: Define possible attendance statuses

**Table**: `attendance_statuses`

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGINT | PK, Auto | Primary key |
| tenant_id | VARCHAR(50) | NOT NULL | Multi-tenancy |
| status_code | VARCHAR(50) | NOT NULL | Unique status identifier |
| status_name | VARCHAR(100) | NOT NULL | Status display name |
| status_type | VARCHAR(20) | NOT NULL | Type: PRESENT, ABSENT, LEAVE, OTHER |
| description | TEXT | | Status description |
| is_active | BOOLEAN | DEFAULT true | Active/Inactive |
| created_at | TIMESTAMP | Auto | Creation timestamp |
| updated_at | TIMESTAMP | Auto | Update timestamp |

**Constraints**:
- UNIQUE: (tenant_id, status_code)

**Example Data**:
```
status_code=PRESENT, status_name=Present, status_type=PRESENT
status_code=ABSENT, status_name=Absent (Unexcused), status_type=ABSENT
status_code=HALF_DAY, status_name=Half Day, status_type=PRESENT
status_code=LATE, status_name=Late Arrival, status_type=PRESENT
status_code=EARLY_LEAVE, status_name=Early Leave, status_type=PRESENT
status_code=ANNUAL_LEAVE, status_name=Annual Leave, status_type=LEAVE
status_code=SICK_LEAVE, status_name=Sick Leave, status_type=LEAVE
status_code=CASUAL_LEAVE, status_name=Casual Leave, status_type=LEAVE
status_code=MATERNITY_LEAVE, status_name=Maternity Leave, status_type=LEAVE
status_code=WEEKEND, status_name=Weekend, status_type=OTHER
status_code=HOLIDAY, status_name=Holiday, status_type=OTHER
```

---

### 3. Attendance Entity
**Purpose**: Track daily attendance records

**Table**: `attendances`

**Columns**:

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGINT | PK, Auto | Primary key |
| tenant_id | VARCHAR(50) | NOT NULL | Multi-tenancy |
| employee_id | BIGINT | FK, NOT NULL | Reference to employees |
| shift_timing_id | BIGINT | FK | Assigned shift for the day |
| attendance_date | DATE | NOT NULL | Attendance date |
| check_in_time | TIME | | Check-in time |
| check_out_time | TIME | | Check-out time |
| attendance_status_id | BIGINT | FK, NOT NULL | Reference to attendance_statuses |
| actual_working_hours | DECIMAL(5,2) | | Hours actually worked |
| remarks | TEXT | | Comments/notes |
| marked_by_user_id | BIGINT | FK | User who marked attendance (admin/manager) |
| marked_at | TIMESTAMP | | When marked by admin |
| is_active | BOOLEAN | DEFAULT true | Active/Inactive |
| created_at | TIMESTAMP | Auto | Creation timestamp |
| updated_at | TIMESTAMP | Auto | Update timestamp |

**Indexes**:
- `idx_tenantid_empid_date` on (tenant_id, employee_id, attendance_date) - CRITICAL
- `idx_empid_date_range` on (employee_id, attendance_date)
- `idx_tenantid_status` on (tenant_id, attendance_status_id)
- `idx_attendance_date` on (attendance_date)

**Constraints**:
- UNIQUE: (tenant_id, employee_id, attendance_date) - One attendance record per employee per day
- FK: employee_id → employees(id)
- FK: shift_timing_id → shift_timings(id)
- FK: attendance_status_id → attendance_statuses(id)
- FK: marked_by_user_id → users(id)

---

### 4. AttendanceReport Entity (Summary Table)
**Purpose**: Store monthly attendance summaries for quick reporting

**Table**: `attendance_reports`

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGINT | PK, Auto | Primary key |
| tenant_id | VARCHAR(50) | NOT NULL | Multi-tenancy |
| employee_id | BIGINT | FK, NOT NULL | Reference to employees |
| year_month | VARCHAR(7) | NOT NULL | YYYY-MM format |
| total_working_days | INT | | Expected working days |
| total_present_days | INT | | Days marked present |
| total_absent_days | INT | | Days marked absent |
| total_leave_days | INT | | Leave days taken |
| total_half_days | INT | | Half days |
| total_late_arrivals | INT | | Late arrivals |
| total_early_leaves | INT | | Early leaves |
| total_working_hours | DECIMAL(8,2) | | Total hours worked |
| expected_working_hours | DECIMAL(8,2) | | Expected hours |
| generated_at | TIMESTAMP | Auto | Report generation time |
| created_at | TIMESTAMP | Auto | Creation timestamp |
| updated_at | TIMESTAMP | Auto | Update timestamp |

**Indexes**:
- `idx_tenantid_empid_yearmonth` on (tenant_id, employee_id, year_month)
- `idx_year_month` on (year_month)

**Constraints**:
- UNIQUE: (tenant_id, employee_id, year_month)
- FK: employee_id → employees(id)

---

## 🔗 Entity Relationships

```
Employees (existing)
    ├── 1:N → Attendances
    └── 1:N → AttendanceReports

ShiftTimings
    ├── 1:N → Attendances
    └── Master data

AttendanceStatuses
    ├── 1:N → Attendances
    └── Master data

Users (existing)
    └── 1:N → Attendances (marked_by)

Attendances
    ├── N:1 → Employees
    ├── N:1 → ShiftTimings
    ├── N:1 → AttendanceStatuses
    └── N:1 → Users

AttendanceReports
    └── N:1 → Employees
```

---

## 📡 GraphQL API Design

### 1. Types Definition (attendance.graphqls)

```graphql
# Input Types
input ShiftTimingInput {
    shiftCode: String!
    shiftName: String!
    startTime: Time!
    endTime: Time!
    workingHours: Float!
    breakDuration: Int
}

input AttendanceStatusInput {
    statusCode: String!
    statusName: String!
    statusType: String!
    description: String
}

input AttendanceInput {
    employeeId: Long!
    shiftTimingId: Long
    attendanceDate: Date!
    checkInTime: Time
    checkOutTime: Time
    attendanceStatusId: Long!
    actualWorkingHours: Float
    remarks: String
}

input AttendanceFilterInput {
    employeeId: Long
    departmentId: Long
    companyId: Long
    fromDate: Date
    toDate: Date
    statusId: Long
    tenantId: String!
    page: Int
    size: Int
    sortBy: String
    sortDirection: String
}

# Output Types
type ShiftTiming {
    id: Long!
    tenantId: String!
    shiftCode: String!
    shiftName: String!
    startTime: Time!
    endTime: Time!
    workingHours: Float!
    breakDuration: Int
    isActive: Boolean!
    createdAt: LocalDateTime!
    updatedAt: LocalDateTime!
}

type AttendanceStatus {
    id: Long!
    tenantId: String!
    statusCode: String!
    statusName: String!
    statusType: String!
    description: String
    isActive: Boolean!
    createdAt: LocalDateTime!
    updatedAt: LocalDateTime!
}

type Attendance {
    id: Long!
    tenantId: String!
    employeeId: Long!
    employee: Employee
    shiftTiming: ShiftTiming
    attendanceDate: Date!
    checkInTime: Time
    checkOutTime: Time
    attendanceStatus: AttendanceStatus!
    actualWorkingHours: Float
    remarks: String
    markedByUser: User
    markedAt: LocalDateTime
    isActive: Boolean!
    createdAt: LocalDateTime!
    updatedAt: LocalDateTime!
}

type AttendanceReport {
    id: Long!
    tenantId: String!
    employeeId: Long!
    yearMonth: String!
    totalWorkingDays: Int!
    totalPresentDays: Int!
    totalAbsentDays: Int!
    totalLeaveDays: Int!
    totalHalfDays: Int!
    totalLateArrivals: Int!
    totalEarlyLeaves: Int!
    totalWorkingHours: Float!
    expectedWorkingHours: Float!
    generatedAt: LocalDateTime!
    createdAt: LocalDateTime!
    updatedAt: LocalDateTime!
}

# Connection Types (for pagination)
type AttendancePage {
    content: [Attendance!]!
    pageNumber: Int!
    pageSize: Int!
    totalElements: Long!
    totalPages: Int!
    isFirst: Boolean!
    isLast: Boolean!
}

type ReportPage {
    content: [AttendanceReport!]!
    pageNumber: Int!
    pageSize: Int!
    totalElements: Long!
    totalPages: Int!
    isFirst: Boolean!
    isLast: Boolean!
}
```

### 2. Queries

```graphql
type Query {
    # Shift Timing Queries
    shiftTimings(tenantId: String!): [ShiftTiming!]!
    shiftTimingById(id: Long!): ShiftTiming
    shiftTimingByCode(tenantId: String!, shiftCode: String!): ShiftTiming

    # Attendance Status Queries
    attendanceStatuses(tenantId: String!): [AttendanceStatus!]!
    attendanceStatusById(id: Long!): AttendanceStatus

    # Attendance Queries
    attendanceById(id: Long!): Attendance
    attendanceByEmployeeAndDate(employeeId: Long!, attendanceDate: Date!): Attendance
    attendancesByEmployee(employeeId: Long!, fromDate: Date, toDate: Date, page: Int, size: Int): AttendancePage!
    attendancesByTenant(tenantId: String!, fromDate: Date, toDate: Date, page: Int, size: Int): AttendancePage!
    attendancesByDepartment(departmentId: Long!, fromDate: Date, toDate: Date, page: Int, size: Int): AttendancePage!
    filteredAttendances(filter: AttendanceFilterInput!): AttendancePage!

    # Report Queries
    attendanceReportByEmployeeAndMonth(employeeId: Long!, yearMonth: String!): AttendanceReport
    attendanceReportsByEmployee(employeeId: Long!, fromYearMonth: String, toYearMonth: String): [AttendanceReport!]!
    attendanceReportsByDepartment(departmentId: Long!, yearMonth: String!): [AttendanceReport!]!
    attendanceReportsByTenant(tenantId: String!, yearMonth: String!): [AttendanceReport!]!

    # Statistics
    attendanceSummary(tenantId: String!, fromDate: Date, toDate: Date): AttendanceSummary!
    departmentAttendanceStats(departmentId: Long!, yearMonth: String!): [DepartmentAttendanceStats!]!
}
```

### 3. Mutations

```graphql
type Mutation {
    # Shift Timing Mutations
    createShiftTiming(input: ShiftTimingInput!): ShiftTiming!
    updateShiftTiming(id: Long!, input: ShiftTimingInput!): ShiftTiming!
    deleteShiftTiming(id: Long!): Boolean!

    # Attendance Status Mutations
    createAttendanceStatus(input: AttendanceStatusInput!): AttendanceStatus!
    updateAttendanceStatus(id: Long!, input: AttendanceStatusInput!): AttendanceStatus!
    deleteAttendanceStatus(id: Long!): Boolean!

    # Attendance Mutations
    createAttendance(input: AttendanceInput!): Attendance!
    updateAttendance(id: Long!, input: AttendanceInput!): Attendance!
    deleteAttendance(id: Long!): Boolean!
    markAttendance(employeeId: Long!, attendanceDate: Date!, statusId: Long!, remarks: String): Attendance!
    markBulkAttendance(tenantId: String!, attendanceDate: Date!, attendances: [AttendanceInput!]!): [Attendance!]!

    # Report Mutations
    generateMonthlyReport(tenantId: String!, yearMonth: String!): AttendanceReport!
    generateMonthlyReportForDepartment(departmentId: Long!, yearMonth: String!): [AttendanceReport!]!
}
```

---

## 🔐 Security & Multi-Tenancy

### Tenant Isolation
- Every attendance record must have `tenantId`
- All queries filtered by user's tenant
- Database constraints enforce tenant isolation

### Role-Based Access Control
**Admin/Manager**:
- View all attendance records for their department/company
- Mark/edit attendance for employees
- Generate reports
- View attendance analytics

**Employee**:
- View only their own attendance
- Submit self-check-in/check-out (future: biometric integration)
- View their own reports

---

## 📋 Implementation Phases

### Phase 1: Setup & Entities (Current)
- [ ] Create JPA entities (ShiftTiming, AttendanceStatus, Attendance, AttendanceReport)
- [ ] Create database repositories
- [ ] Create data seeder with sample data

### Phase 2: Service Layer
- [ ] Create AttendanceService interface
- [ ] Implement AttendanceServiceImpl
- [ ] Implement filtering with Criteria API
- [ ] Implement report generation logic

### Phase 3: GraphQL API
- [ ] Create attendance.graphqls schema
- [ ] Create AttendanceResolver with all queries/mutations
- [ ] Implement GraphQL logging with LoggingUtil

### Phase 4: Testing
- [ ] Create unit tests for services
- [ ] Create integration tests for GraphQL endpoints
- [ ] Create attendance test data

### Phase 5: Enhancements (Future)
- [ ] Attendance analytics dashboard
- [ ] Biometric integration
- [ ] Leave request workflows
- [ ] Email notifications

---

## 📚 Code Structure

```
src/main/java/com/hrms/
├── entity/
│   ├── ShiftTiming.java          (NEW)
│   ├── AttendanceStatus.java     (NEW)
│   ├── Attendance.java           (NEW)
│   └── AttendanceReport.java     (NEW)
├── dto/
│   ├── request/
│   │   ├── ShiftTimingInput.java        (NEW)
│   │   ├── AttendanceStatusInput.java   (NEW)
│   │   ├── AttendanceInput.java         (NEW)
│   │   └── AttendanceFilterInput.java   (NEW)
│   └── response/
│       ├── AttendancePage.java         (NEW)
│       └── ReportPage.java             (NEW)
├── repository/
│   ├── ShiftTimingRepository.java      (NEW)
│   ├── AttendanceStatusRepository.java (NEW)
│   ├── AttendanceRepository.java       (NEW)
│   └── AttendanceReportRepository.java (NEW)
├── service/
│   ├── AttendanceService.java          (NEW)
│   └── impl/
│       └── AttendanceServiceImpl.java   (NEW)
├── graphql/
│   ├── input/
│   │   ├── ShiftTimingInput.java       (NEW)
│   │   ├── AttendanceStatusInput.java  (NEW)
│   │   ├── AttendanceInput.java        (NEW)
│   │   └── AttendanceFilterInput.java  (NEW)
│   └── resolver/
│       └── AttendanceResolver.java     (NEW)
└── mapper/
    └── AttendanceMapper.java           (NEW - MapStruct)

src/main/resources/
└── graphql/
    └── attendance.graphqls             (NEW)

src/main/java/com/hrms/config/
└── AttendanceDataInitializer.java      (NEW - Sample data)
```

---

## 🗂️ Files to Create

**1. Entities (4 files)**
```
src/main/java/com/hrms/entity/ShiftTiming.java
src/main/java/com/hrms/entity/AttendanceStatus.java
src/main/java/com/hrms/entity/Attendance.java
src/main/java/com/hrms/entity/AttendanceReport.java
```

**2. DTOs (6 files)**
```
src/main/java/com/hrms/dto/request/ShiftTimingInput.java
src/main/java/com/hrms/dto/request/AttendanceStatusInput.java
src/main/java/com/hrms/dto/request/AttendanceInput.java
src/main/java/com/hrms/dto/request/AttendanceFilterInput.java
src/main/java/com/hrms/dto/response/AttendancePage.java
src/main/java/com/hrms/dto/response/ReportPage.java
```

**3. Repositories (4 files)**
```
src/main/java/com/hrms/repository/ShiftTimingRepository.java
src/main/java/com/hrms/repository/AttendanceStatusRepository.java
src/main/java/com/hrms/repository/AttendanceRepository.java
src/main/java/com/hrms/repository/AttendanceReportRepository.java
```

**4. Services (2 files)**
```
src/main/java/com/hrms/service/AttendanceService.java
src/main/java/com/hrms/service/impl/AttendanceServiceImpl.java
```

**5. Resolvers (1 file)**
```
src/main/java/com/hrms/graphql/resolver/AttendanceResolver.java
```

**6. Mappers (1 file)**
```
src/main/java/com/hrms/mapper/AttendanceMapper.java
```

**7. GraphQL Schema (1 file)**
```
src/main/resources/graphql/attendance.graphqls
```

**8. Data Initializer (1 file)**
```
src/main/java/com/hrms/config/AttendanceDataInitializer.java
```

**9. Documentation (1 file)**
```
docs/2025-Dec-03/4-attendance-module-implementation.md (will update after implementation)
```

**Total**: 20 files to create

---

## 🚀 Next Steps

1. **Create all 4 JPA entities** with proper annotations
2. **Create 4 repositories** for data access
3. **Create service interface and implementation** with business logic
4. **Create GraphQL resolver** with all queries/mutations
5. **Create GraphQL schema** file (attendance.graphqls)
6. **Create MapStruct mapper** for DTO conversions
7. **Create data seeder** with sample shift timings and statuses
8. **Test GraphQL API** with GraphiQL
9. **Document implementation** and commit

---

## 📝 Key Design Decisions

1. **Attendance Records**: One record per employee per day (UNIQUE constraint)
2. **Filtering**: Support advanced filtering with Criteria API (like employee module)
3. **Reports**: Separate table for monthly summaries (performance optimization)
4. **Status Types**: Generic status system for flexibility (Present, Absent, Leave, etc.)
5. **Multi-Tenancy**: Every table has tenantId (mandatory)
6. **Logging**: All resolvers will use LoggingUtil for consistent logging

---

**Branch**: `feature-attendance-module`
**Status**: ✅ Planning Complete - Ready for Implementation
**Created**: 2025-Dec-03
**Model**: Claude Haiku 4.5
