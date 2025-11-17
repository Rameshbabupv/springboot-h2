# HRMS SaaS Application

A simple Spring Boot HRMS (Human Resource Management System) application with H2 in-memory database.

## Features

- Multi-tenant HRMS system
- In-memory H2 database with sample data
- **GraphQL API** for efficient data fetching (React integration ready)
- REST APIs for managing:
  - Companies
  - Departments
  - Designations
  - Employees

## Tech Stack

- Spring Boot 3.2.0
- Spring for GraphQL
- Spring Data JPA
- H2 Database
- Lombok
- Java 17

## How to Run

### Build the application
```bash
mvn clean package
```

### Run the application
```bash
java -jar target/hrms-saas-1.0.0.jar
```

Or using Maven:
```bash
mvn spring-boot:run
```

The application will start on port 8080.

## GraphQL API (Recommended for React)

**GraphQL Endpoint**: `http://localhost:8080/graphql`
**GraphiQL IDE**: `http://localhost:8080/graphiql`

### Quick GraphQL Examples

**Query all employees:**
```graphql
{
  employees {
    id
    empId
    employeeName
    emailId
    department { name }
    designation { name }
  }
}
```

**Create a department:**
```graphql
mutation {
  createDepartment(input: {
    tenantId: "TENANT001"
    name: "Sales"
    code: "SALES"
    description: "Sales Department"
    isActive: true
  }) {
    id
    name
    code
  }
}
```

**For complete React integration guide, see [GRAPHQL_REACT_INTEGRATION.md](GRAPHQL_REACT_INTEGRATION.md)**

## REST API Endpoints

### Companies
- `GET /api/companies` - Get all companies
- `GET /api/companies/{id}` - Get company by ID
- `GET /api/companies/tenant/{tenantId}` - Get companies by tenant
- `GET /api/companies/active` - Get active companies
- `POST /api/companies` - Create new company
- `PUT /api/companies/{id}` - Update company
- `DELETE /api/companies/{id}` - Delete company

### Departments
- `GET /api/departments` - Get all departments
- `GET /api/departments/{id}` - Get department by ID
- `GET /api/departments/tenant/{tenantId}` - Get departments by tenant
- `GET /api/departments/active` - Get active departments
- `POST /api/departments` - Create new department
- `PUT /api/departments/{id}` - Update department
- `DELETE /api/departments/{id}` - Delete department

### Designations
- `GET /api/designations` - Get all designations
- `GET /api/designations/{id}` - Get designation by ID
- `GET /api/designations/tenant/{tenantId}` - Get designations by tenant
- `GET /api/designations/active` - Get active designations
- `POST /api/designations` - Create new designation
- `PUT /api/designations/{id}` - Update designation
- `DELETE /api/designations/{id}` - Delete designation

### Employees
- `GET /api/employees` - Get all employees
- `GET /api/employees/{id}` - Get employee by ID
- `GET /api/employees/tenant/{tenantId}` - Get employees by tenant
- `GET /api/employees/company/{companyId}` - Get employees by company
- `GET /api/employees/department/{departmentId}` - Get employees by department
- `GET /api/employees/active` - Get active employees
- `GET /api/employees/status/{status}` - Get employees by status
- `POST /api/employees` - Create new employee
- `PUT /api/employees/{id}` - Update employee
- `DELETE /api/employees/{id}` - Delete employee

## H2 Console

Access H2 console at: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:hrmsdb`
- Username: `sa`
- Password: (leave blank)

## Sample Data

The application comes with pre-loaded sample data:
- 2 Companies (Tech Solutions Pvt Ltd, Manufacturing Corp)
- 3 Departments (Engineering, Human Resources, Finance)
- 4 Designations (Software Engineer, Senior Software Engineer, HR Manager, Finance Manager)
- 5 Employees with realistic data

## Example API Calls

### Get all employees
```bash
curl http://localhost:8080/api/employees
```

### Get employees by company
```bash
curl http://localhost:8080/api/employees/company/1
```

### Create a new department
```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "TENANT001",
    "name": "Marketing",
    "code": "MKT",
    "description": "Marketing Department",
    "isActive": true
  }'
```

## Project Structure

```
src/
├── main/
│   ├── java/com/hrms/
│   │   ├── HrmsApplication.java
│   │   ├── DataInitializer.java
│   │   ├── entity/
│   │   │   ├── Company.java
│   │   │   ├── Department.java
│   │   │   ├── Designation.java
│   │   │   └── Employee.java
│   │   ├── repository/
│   │   │   ├── CompanyRepository.java
│   │   │   ├── DepartmentRepository.java
│   │   │   ├── DesignationRepository.java
│   │   │   └── EmployeeRepository.java
│   │   ├── controller/
│   │   │   ├── CompanyController.java
│   │   │   ├── DepartmentController.java
│   │   │   ├── DesignationController.java
│   │   │   └── EmployeeController.java
│   │   └── graphql/
│   │       ├── resolver/
│   │       │   ├── CompanyResolver.java
│   │       │   ├── DepartmentResolver.java
│   │       │   ├── DesignationResolver.java
│   │       │   └── EmployeeResolver.java
│   │       └── input/
│   │           ├── CompanyInput.java
│   │           ├── DepartmentInput.java
│   │           ├── DesignationInput.java
│   │           └── EmployeeInput.java
│   └── resources/
│       ├── application.properties
│       └── graphql/
│           └── schema.graphqls
```

## Documentation

- **[GRAPHQL_REACT_INTEGRATION.md](GRAPHQL_REACT_INTEGRATION.md)** - Complete guide for integrating with React frontend using Apollo Client
