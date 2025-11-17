# GraphQL React Integration Guide

## Overview

The HRMS application now supports GraphQL for efficient data fetching from your React frontend. GraphQL is available at `/graphql` endpoint with GraphiQL IDE at `/graphiql`.

## Endpoints

- **GraphQL API**: `http://localhost:8080/graphql`
- **GraphiQL IDE**: `http://localhost:8080/graphiql` (Interactive GraphQL playground)

## React Setup

### 1. Install Dependencies

```bash
npm install @apollo/client graphql
# or
yarn add @apollo/client graphql
```

### 2. Configure Apollo Client

Create `src/apollo/client.js`:

```javascript
import { ApolloClient, InMemoryCache, HttpLink } from '@apollo/client';

const httpLink = new HttpLink({
  uri: 'http://localhost:8080/graphql',
});

const client = new ApolloClient({
  link: httpLink,
  cache: new InMemoryCache(),
  defaultOptions: {
    watchQuery: {
      fetchPolicy: 'cache-and-network',
    },
  },
});

export default client;
```

### 3. Wrap App with Apollo Provider

Update `src/main.jsx` or `src/index.jsx`:

```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';
import { ApolloProvider } from '@apollo/client';
import client from './apollo/client';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ApolloProvider client={client}>
      <App />
    </ApolloProvider>
  </React.StrictMode>
);
```

## GraphQL Queries & Mutations Examples

### Queries

#### 1. Fetch All Employees

```javascript
import { gql, useQuery } from '@apollo/client';

const GET_EMPLOYEES = gql`
  query GetEmployees {
    employees {
      id
      empId
      employeeName
      emailId
      mobileNo
      department {
        id
        name
      }
      designation {
        id
        name
      }
      company {
        id
        companyName
      }
    }
  }
`;

function EmployeeList() {
  const { loading, error, data } = useQuery(GET_EMPLOYEES);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>Error: {error.message}</p>;

  return (
    <div>
      {data.employees.map((employee) => (
        <div key={employee.id}>
          <h3>{employee.employeeName}</h3>
          <p>ID: {employee.empId}</p>
          <p>Email: {employee.emailId}</p>
          <p>Department: {employee.department?.name}</p>
          <p>Designation: {employee.designation?.name}</p>
        </div>
      ))}
    </div>
  );
}
```

#### 2. Fetch Employees by Company

```javascript
const GET_EMPLOYEES_BY_COMPANY = gql`
  query GetEmployeesByCompany($companyId: ID!) {
    employeesByCompany(companyId: $companyId) {
      id
      empId
      employeeName
      emailId
      designation {
        name
      }
    }
  }
`;

function CompanyEmployees({ companyId }) {
  const { loading, error, data } = useQuery(GET_EMPLOYEES_BY_COMPANY, {
    variables: { companyId },
  });

  // ... render logic
}
```

#### 3. Fetch All Companies

```javascript
const GET_COMPANIES = gql`
  query GetCompanies {
    companies {
      id
      companyName
      industry
      city
      state
      email
      primaryPhone
    }
  }
`;
```

#### 4. Fetch Departments

```javascript
const GET_DEPARTMENTS = gql`
  query GetDepartments {
    departments {
      id
      name
      code
      description
      isActive
    }
  }
`;
```

#### 5. Fetch Active Departments

```javascript
const GET_ACTIVE_DEPARTMENTS = gql`
  query GetActiveDepartments {
    activeDepartments {
      id
      name
      code
    }
  }
`;
```

### Mutations

#### 1. Create Employee

```javascript
import { gql, useMutation } from '@apollo/client';

const CREATE_EMPLOYEE = gql`
  mutation CreateEmployee($input: EmployeeInput!) {
    createEmployee(input: $input) {
      id
      empId
      employeeName
      emailId
    }
  }
`;

function CreateEmployeeForm() {
  const [createEmployee, { loading, error }] = useMutation(CREATE_EMPLOYEE, {
    refetchQueries: [{ query: GET_EMPLOYEES }],
  });

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const result = await createEmployee({
        variables: {
          input: {
            tenantId: "TENANT001",
            companyId: "1",
            empId: "EMP006",
            employeeName: "John Doe",
            dateOfJoin: "2024-01-01",
            emailId: "john.doe@example.com",
            mobileNo: "9876543215",
            departmentId: "1",
            designationId: "1",
            isActive: true,
            coverPf: false,
            coverEsi: false,
          },
        },
      });
      console.log('Employee created:', result.data.createEmployee);
    } catch (err) {
      console.error('Error creating employee:', err);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
      <button type="submit" disabled={loading}>
        {loading ? 'Creating...' : 'Create Employee'}
      </button>
      {error && <p>Error: {error.message}</p>}
    </form>
  );
}
```

#### 2. Update Employee

```javascript
const UPDATE_EMPLOYEE = gql`
  mutation UpdateEmployee($id: ID!, $input: EmployeeInput!) {
    updateEmployee(id: $id, input: $input) {
      id
      employeeName
      emailId
    }
  }
`;

function UpdateEmployeeForm({ employeeId }) {
  const [updateEmployee] = useMutation(UPDATE_EMPLOYEE);

  const handleUpdate = async (updatedData) => {
    await updateEmployee({
      variables: {
        id: employeeId,
        input: updatedData,
      },
    });
  };

  // ... form logic
}
```

#### 3. Create Department

```javascript
const CREATE_DEPARTMENT = gql`
  mutation CreateDepartment($input: DepartmentInput!) {
    createDepartment(input: $input) {
      id
      name
      code
    }
  }
`;

function CreateDepartment() {
  const [createDepartment] = useMutation(CREATE_DEPARTMENT);

  const handleCreate = async () => {
    await createDepartment({
      variables: {
        input: {
          tenantId: "TENANT001",
          name: "Marketing",
          code: "MKT",
          description: "Marketing Department",
          isActive: true,
        },
      },
    });
  };

  // ... render logic
}
```

#### 4. Delete Employee

```javascript
const DELETE_EMPLOYEE = gql`
  mutation DeleteEmployee($id: ID!) {
    deleteEmployee(id: $id)
  }
`;

function DeleteEmployeeButton({ employeeId }) {
  const [deleteEmployee] = useMutation(DELETE_EMPLOYEE, {
    refetchQueries: [{ query: GET_EMPLOYEES }],
  });

  const handleDelete = async () => {
    if (window.confirm('Are you sure?')) {
      await deleteEmployee({
        variables: { id: employeeId },
      });
    }
  };

  return <button onClick={handleDelete}>Delete</button>;
}
```

## Advanced Usage

### Using with React Hook Form

```javascript
import { useForm } from 'react-hook-form';
import { useMutation } from '@apollo/client';

function EmployeeForm() {
  const { register, handleSubmit } = useForm();
  const [createEmployee] = useMutation(CREATE_EMPLOYEE);

  const onSubmit = async (data) => {
    await createEmployee({
      variables: {
        input: {
          ...data,
          tenantId: "TENANT001",
        },
      },
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input {...register('employeeName')} placeholder="Name" />
      <input {...register('emailId')} placeholder="Email" />
      {/* ... more fields */}
      <button type="submit">Submit</button>
    </form>
  );
}
```

### Pagination Example

```javascript
const GET_EMPLOYEES_PAGINATED = gql`
  query GetEmployees($limit: Int, $offset: Int) {
    employees(limit: $limit, offset: $offset) {
      id
      employeeName
      emailId
    }
  }
`;

function PaginatedEmployees() {
  const [page, setPage] = useState(0);
  const pageSize = 10;

  const { data, loading } = useQuery(GET_EMPLOYEES_PAGINATED, {
    variables: {
      limit: pageSize,
      offset: page * pageSize,
    },
  });

  // ... render with pagination controls
}
```

### Error Handling

```javascript
function EmployeeList() {
  const { loading, error, data } = useQuery(GET_EMPLOYEES, {
    onError: (error) => {
      console.error('GraphQL Error:', error);
      // Handle error (show toast, etc.)
    },
  });

  if (loading) return <Spinner />;
  if (error) return <ErrorComponent message={error.message} />;

  return <div>{/* render data */}</div>;
}
```

## Available Queries

### Companies
- `companies` - All companies
- `company(id: ID!)` - Single company
- `companiesByTenant(tenantId: String!)` - Companies by tenant
- `activeCompanies` - Active companies only

### Departments
- `departments` - All departments
- `department(id: ID!)` - Single department
- `departmentsByTenant(tenantId: String!)` - Departments by tenant
- `activeDepartments` - Active departments only

### Designations
- `designations` - All designations
- `designation(id: ID!)` - Single designation
- `designationsByTenant(tenantId: String!)` - Designations by tenant
- `activeDesignations` - Active designations only

### Employees
- `employees` - All employees
- `employee(id: ID!)` - Single employee
- `employeesByTenant(tenantId: String!)` - Employees by tenant
- `employeesByCompany(companyId: ID!)` - Employees by company
- `employeesByDepartment(departmentId: ID!)` - Employees by department
- `activeEmployees` - Active employees only
- `employeesByStatus(status: String!)` - Employees by status

## Available Mutations

### Companies
- `createCompany(input: CompanyInput!): Company!`
- `updateCompany(id: ID!, input: CompanyInput!): Company!`
- `deleteCompany(id: ID!): Boolean!`

### Departments
- `createDepartment(input: DepartmentInput!): Department!`
- `updateDepartment(id: ID!, input: DepartmentInput!): Department!`
- `deleteDepartment(id: ID!): Boolean!`

### Designations
- `createDesignation(input: DesignationInput!): Designation!`
- `updateDesignation(id: ID!, input: DesignationInput!): Designation!`
- `deleteDesignation(id: ID!): Boolean!`

### Employees
- `createEmployee(input: EmployeeInput!): Employee!`
- `updateEmployee(id: ID!, input: EmployeeInput!): Employee!`
- `deleteEmployee(id: ID!): Boolean!`

## Testing with GraphiQL

Open http://localhost:8080/graphiql in your browser to test queries interactively.

Example query in GraphiQL:
```graphql
query {
  employees {
    id
    employeeName
    emailId
    department {
      name
    }
    designation {
      name
    }
  }
}
```

Example mutation in GraphiQL:
```graphql
mutation {
  createDepartment(input: {
    tenantId: "TENANT001"
    name: "IT Support"
    code: "IT"
    description: "IT Support Team"
    isActive: true
  }) {
    id
    name
    code
  }
}
```

## CORS Configuration

The backend is already configured to accept requests from:
- `http://localhost:3000` (Create React App)
- `http://localhost:5173` (Vite)

If you need to add more origins, update `application.properties`:
```properties
spring.graphql.cors.allowed-origins=http://localhost:3000,http://localhost:5173,http://your-domain.com
```

## Best Practices

1. **Use Fragments** for reusable field selections
2. **Cache Management** - Configure InMemoryCache properly
3. **Error Handling** - Always handle loading and error states
4. **Refetch Queries** - Use refetchQueries or update cache after mutations
5. **Type Safety** - Consider using GraphQL Code Generator for TypeScript types
6. **Batching** - Use Apollo Link for request batching if needed

## TypeScript Support

Install GraphQL Code Generator:
```bash
npm install -D @graphql-codegen/cli @graphql-codegen/typescript @graphql-codegen/typescript-operations @graphql-codegen/typescript-react-apollo
```

Create `codegen.yml`:
```yaml
schema: http://localhost:8080/graphql
documents: './src/**/*.tsx'
generates:
  ./src/generated/graphql.tsx:
    plugins:
      - typescript
      - typescript-operations
      - typescript-react-apollo
```

Run:
```bash
npx graphql-codegen
```

This generates fully typed hooks for your queries and mutations!
