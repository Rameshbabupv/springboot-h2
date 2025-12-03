# Master Data Tables - South Indian Context

## Successfully Implemented ✅

All master data tables have been created with comprehensive South Indian mock data.

### 1. States (5 records)
| ID | Name | Code | Description |
|----|------|------|-------------|
| 1 | Tamil Nadu | TN | Southernmost state |
| 2 | Karnataka | KA | Silicon Valley of India |
| 3 | Kerala | KL | God's Own Country |
| 4 | Andhra Pradesh | AP | Rice bowl of India |
| 5 | Telangana | TS | State of the Telugus |

### 2. Cities (10 records)
| ID | City Name | State | Pincode |
|----|-----------|-------|---------|
| 1 | Chennai | Tamil Nadu | 600001 |
| 2 | Coimbatore | Tamil Nadu | 641001 |
| 3 | Madurai | Tamil Nadu | 625001 |
| 4 | Bangalore | Karnataka | 560001 |
| 5 | Mysore | Karnataka | 570001 |
| 6 | Kochi | Kerala | 682001 |
| 7 | Trivandrum | Kerala | 695001 |
| 8 | Hyderabad | Telangana | 500001 |
| 9 | Vijayawada | Andhra Pradesh | 520001 |
| 10 | Visakhapatnam | Andhra Pradesh | 530001 |

### 3. Divisions (4 records)
| ID | Name | Code | Description |
|----|------|------|-------------|
| 1 | South Division | SOUTH | Southern region operations |
| 2 | North Division | NORTH | Northern region operations |
| 3 | Technology Division | TECH | Technology and Innovation |
| 4 | Operations Division | OPS | Business Operations |

### 4. Departments (5 records)
| ID | Name | Code | Description |
|----|------|------|-------------|
| 1 | Engineering | ENG | Software Engineering and Development |
| 2 | Human Resources | HR | Human Resources Management |
| 3 | Finance | FIN | Finance and Accounts |
| 4 | Sales | SALES | Sales and Business Development |
| 5 | Marketing | MKT | Marketing and Branding |

### 5. Sections (5 records)
| ID | Name | Code | Department | Description |
|----|------|------|------------|-------------|
| 1 | Frontend Development | FE | Engineering | UI/UX Development |
| 2 | Backend Development | BE | Engineering | Server-side Development |
| 3 | DevOps | DEVOPS | Engineering | Infrastructure and Deployment |
| 4 | Recruitment | REC | Human Resources | Talent Acquisition |
| 5 | Payroll | PAY | Human Resources | Payroll Processing |

### 6. Job Functions (4 records)
| ID | Name | Code | Description |
|----|------|------|-------------|
| 1 | Software Development | DEV | Software Development |
| 2 | Management | MGMT | People and Project Management |
| 3 | Support | SUP | Technical and Customer Support |
| 4 | Administration | ADMIN | Administrative Functions |

### 7. Employment Types (4 records)
| ID | Name | Code | Description |
|----|------|------|-------------|
| 1 | Permanent | PERM | Full-time Permanent |
| 2 | Contract | CONT | Fixed-term Contract |
| 3 | Intern | INTERN | Internship |
| 4 | Consultant | CONS | Consultant |

### 8. Grades (5 records)
| ID | Name | Code | Level | Min Salary | Max Salary | Description |
|----|------|------|-------|------------|------------|-------------|
| 1 | Grade G1 | G1 | 1 | ₹3,00,000 | ₹5,00,000 | Entry Level |
| 2 | Grade G2 | G2 | 2 | ₹5,00,000 | ₹8,00,000 | Junior Level |
| 3 | Grade G3 | G3 | 3 | ₹8,00,000 | ₹12,00,000 | Mid Level |
| 4 | Grade G4 | G4 | 4 | ₹12,00,000 | ₹18,00,000 | Senior Level |
| 5 | Grade G5 | G5 | 5 | ₹18,00,000 | ₹25,00,000 | Lead Level |

### 9. Designations (7 records)
| ID | Name | Code | Description |
|----|------|------|-------------|
| 1 | Software Engineer | SE | Software Development Engineer |
| 2 | Senior Software Engineer | SSE | Senior Software Development Engineer |
| 3 | Tech Lead | TL | Technical Lead |
| 4 | Project Manager | PM | Project Manager |
| 5 | HR Manager | HRM | Human Resources Manager |
| 6 | Finance Manager | FM | Finance and Accounts Manager |
| 7 | Sales Manager | SM | Sales Manager |

### 10. Companies (2 records)
| ID | Name | Short Name | Industry | State | City |
|----|------|------------|----------|-------|------|
| 1 | Chennai Tech Solutions Pvt Ltd | CTS | IT Services | Tamil Nadu | Chennai |
| 2 | Bangalore Manufacturing Corp | BMC | Manufacturing | Karnataka | Bangalore |

### 11. Employees (8 records) - South Indian Names
| ID | Emp ID | Name | Gender | Department | Designation | Email |
|----|--------|------|--------|------------|-------------|-------|
| 1 | EMP001 | Rajesh Kumar | Male | Engineering | Senior Software Engineer | rajesh.kumar@chennaitech.com |
| 2 | EMP002 | Lakshmi Priya | Female | Engineering | Software Engineer | lakshmi.priya@chennaitech.com |
| 3 | EMP003 | Venkatesh Iyer | Male | Human Resources | HR Manager | venkatesh.iyer@chennaitech.com |
| 4 | EMP004 | Sowmya Reddy | Female | Finance | Finance Manager | sowmya.reddy@chennaitech.com |
| 5 | EMP005 | Krishnan Nair | Male | Engineering | Senior Software Engineer | krishnan.nair@bangaloremfg.com |
| 6 | EMP006 | Meenakshi Sundaram | Female | Sales | Sales Manager | meenakshi.sundaram@chennaitech.com |
| 7 | EMP007 | Arjun Ramakrishnan | Male | Engineering | Software Engineer | arjun.ramakrishnan@chennaitech.com |
| 8 | EMP008 | Divya Bharathi | Female | Marketing | Software Engineer | divya.bharathi@chennaitech.com |

## Technical Details

### Database Tables Created
All tables are in H2 in-memory database with proper relationships:

1. ✅ **states** - State master
2. ✅ **cities** - City master (FK: none, state as varchar)
3. ✅ **divisions** - Division master
4. ✅ **departments** - Department master
5. ✅ **sections** - Section master (FK: department_id)
6. ✅ **designations** - Designation master
7. ✅ **job_functions** - Job function master
8. ✅ **employment_types** - Employment type master
9. ✅ **grades** - Salary grade master
10. ✅ **companies** - Company master
11. ✅ **employees** - Employee master (FK: company, department, designation, reporting_manager)

### Repositories Created
- StateRepository
- CityRepository
- DivisionRepository
- DepartmentRepository (existing)
- SectionRepository
- DesignationRepository (existing)
- JobFunctionRepository
- EmploymentTypeRepository
- GradeRepository
- CompanyRepository (existing)
- EmployeeRepository (existing)

### Data Characteristics
- **Tenant**: All data belongs to TENANT001
- **South Indian Context**:
  - Companies in Chennai & Bangalore
  - States: TN, KA, KL, AP, TS
  - Cities: Chennai, Bangalore, Kochi, Hyderabad, etc.
  - Employee names: Lakshmi Priya, Venkatesh Iyer, Krishnan Nair, Sowmya Reddy, etc.
- **Realistic Data**: Proper salary ranges, designations, and organizational structure

## Next Steps (Pending)

1. GraphQL Schema for new master tables
2. GraphQL Resolvers for all master data
3. REST Controllers (optional)
4. Company extension tables (settings, locations, bank accounts)
5. Employee extension tables (documents, education, contacts, history)
6. User management tables (users, roles, permissions)

## Access

**Application**: http://localhost:8080
**H2 Console**: http://localhost:8080/h2-console
**GraphiQL**: http://localhost:8080/graphiql
**REST API**: http://localhost:8080/api/*

## Sample Queries

```bash
# Get all employees
curl http://localhost:8080/api/employees

# Get all departments
curl http://localhost:8080/api/departments

# Get all designations
curl http://localhost:8080/api/designations
```

All master data is fully populated and ready for use!
