# HRMS Product Documentation

## Company Setup Module Documentation

This directory contains comprehensive documentation for the Company Setup module of the HRMS SaaS application.

### Available Documents

| Document | Description | Use Case |
|----------|-------------|----------|
| [Company-Setup-Analytics-Documentation.md](./Company-Setup-Analytics-Documentation.md) | Complete analytics and business documentation | Product managers, business analysts, stakeholders |
| [Company-Setup-Database-Schema.md](./Company-Setup-Database-Schema.md) | Database schema reference with DDL scripts | Database administrators, backend developers |

---

## Quick Navigation

### For Product Managers
Read: **Company-Setup-Analytics-Documentation.md**
- Module overview and features
- Business logic and rules
- Analytics and reporting queries
- Dashboard metrics

### For Developers
Read: **Company-Setup-Database-Schema.md**
- DDL scripts for all tables
- Sample data inserts
- Migration examples
- Performance optimization tips

### For Database Administrators
Read: **Company-Setup-Database-Schema.md**
- Complete schema definitions
- Index recommendations
- Maintenance scripts
- Backup strategies

---

## Module Overview

### Company Setup Components

```
Company Setup Module
│
├── Company Master
│   ├── Basic Information
│   ├── Contact Details
│   └── Address Information
│
├── Statutory Details
│   ├── Tax Identifiers (PAN, TAN, CIN, GSTIN)
│   ├── PF Configuration
│   ├── ESI Configuration
│   └── PT Configuration
│
├── General Settings
│   ├── Organizational Structure
│   ├── System Preferences
│   └── Localization Settings
│
├── Locations (Multiple)
│   ├── Head Office
│   ├── Branch Offices
│   └── Factories/Warehouses
│
└── Bank Accounts (Multiple)
    ├── Current Accounts
    ├── Savings Accounts
    └── Primary Account Management
```

---

## Database Tables

### Core Tables (5)

1. **company** - Parent table with company master data
2. **company_statutory** - Tax and compliance details (1:1)
3. **company_general_settings** - Organization preferences (1:1)
4. **company_location** - Physical locations (1:N)
5. **company_bank_account** - Banking details (1:N)

### Relationships
```
company (1) ←→ (1) company_statutory
company (1) ←→ (1) company_general_settings
company (1) ←→ (N) company_location
company (1) ←→ (N) company_bank_account
```

---

## Quick Reference

### GraphQL Endpoints

**Queries:**
- `companies` - Get all companies
- `company(id)` - Get company by ID
- `companyByCode(code)` - Get company by code
- `companiesByTenant(tenantId)` - Get tenant companies
- `companyStatutory(companyId)` - Get statutory details
- `companyGeneralSettings(companyId)` - Get settings
- `companyLocations(companyId)` - Get all locations
- `companyBankAccounts(companyId)` - Get all bank accounts

**Mutations:**
- `createCompany(input)` - Create new company
- `updateCompany(id, input)` - Update company
- `updateCompanyStatutory(companyId, input)` - Update statutory
- `updateCompanyGeneralSettings(companyId, input)` - Update settings
- `createCompanyLocation(companyId, input)` - Add location
- `createCompanyBankAccount(companyId, input)` - Add bank account
- `setCompanyPrimaryBankAccount(id)` - Set primary account

### REST Endpoints

Base URL: `http://localhost:8090/api`

- `GET /api/companies` - List all companies
- `GET /api/companies/{id}` - Get company by ID
- `POST /api/companies` - Create company
- `PUT /api/companies/{id}` - Update company
- `DELETE /api/companies/{id}` - Delete company

---

## Common Use Cases

### 1. Create Complete Company Setup

```graphql
# Step 1: Create Company
mutation {
  createCompany(input: {
    tenantId: "TENANT001"
    code: "COMP001"
    name: "Acme Corporation"
    industry: "textile"
    industryDescription: "Organic cotton manufacturing"
    email: "contact@acme.com"
    isActive: true
  }) {
    id
  }
}

# Step 2: Update Statutory
mutation {
  updateCompanyStatutory(companyId: 1, input: {
    pan: "AAAAA1234A"
    gstin: "27AAAAA1234A1Z5"
    pfEnabled: true
    esiEnabled: true
  }) {
    id
  }
}

# Step 3: Add Location
mutation {
  createCompanyLocation(companyId: 1, input: {
    type: "Head Office"
    name: "Acme HQ"
    addressLine1: "123 Main Street"
    city: "Mumbai"
    state: "Maharashtra"
    pincode: "400001"
    contactName: "John Doe"
    contactPhone: "9876543210"
  }) {
    id
  }
}

# Step 4: Add Bank Account
mutation {
  createCompanyBankAccount(companyId: 1, input: {
    beneficiaryName: "Acme Corporation"
    accountName: "Acme Corp Ltd"
    bankName: "HDFC Bank"
    accountNumber: "123456789012"
    ifscCode: "HDFC0001234"
    accountType: "Current"
    isPrimary: true
  }) {
    id
  }
}
```

### 2. Get Complete Company Data

```graphql
query {
  company(id: 1) {
    id
    code
    name
    industry
    industryDescription
    statutory {
      pan
      gstin
      pfEnabled
      esiEnabled
    }
    generalSettings {
      currency
      dateFormat
      enableDivisions
    }
    locations {
      name
      type
      city
      state
    }
    bankAccounts {
      accountName
      bankName
      accountNumber
      isPrimary
    }
  }
}
```

### 3. Analytics Query

```sql
-- Company setup completion status
SELECT
    c.id,
    c.name,
    CASE WHEN s.id IS NOT NULL THEN '✓' ELSE '✗' END as has_statutory,
    CASE WHEN gs.id IS NOT NULL THEN '✓' ELSE '✗' END as has_settings,
    COUNT(DISTINCT l.id) as location_count,
    COUNT(DISTINCT b.id) as bank_account_count
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
LEFT JOIN company_general_settings gs ON c.id = gs.company_id
LEFT JOIN company_location l ON c.id = l.company_id AND l.is_active = true
LEFT JOIN company_bank_account b ON c.id = b.company_id AND b.is_active = true
WHERE c.is_active = true
GROUP BY c.id, c.name, s.id, gs.id;
```

---

## Validation Rules

### Company Code
- **Format:** Alphanumeric, max 20 characters
- **Uniqueness:** Must be unique globally
- **Required:** Yes

### Tax Identifiers
- **PAN:** 10 characters (AAAAA9999A)
- **TAN:** 10 characters (AAAA99999A)
- **CIN:** 21 characters
- **GSTIN:** 15 characters (27AAAAA1234A1Z5)

### Phone Numbers
- **Format:** Max 15 characters
- **Example:** +91-9876543210, 9876543210

### Email
- **Format:** Valid email format
- **Max Length:** 100 characters

### IFSC Code
- **Format:** 11 characters (AAAA0BBBBBB)
- **Example:** HDFC0001234

---

## Data Flow

### Company Creation Flow
1. User submits company basic information
2. System validates tenant_id and code uniqueness
3. Company record created with auto-generated ID
4. Default statutory record created (linked via company_id)
5. Default general settings created (linked via company_id)
6. User can add locations and bank accounts

### Primary Bank Account Update Flow
1. User selects a bank account to set as primary
2. System finds existing primary account (if any)
3. Existing primary account's `is_primary` flag set to FALSE
4. New account's `is_primary` flag set to TRUE
5. Only one primary account per company enforced

---

## Performance Tips

### For Queries
1. Always filter by `tenant_id` for multi-tenant queries
2. Use `is_active = true` to exclude soft-deleted records
3. Use pagination for large result sets
4. Index frequently filtered columns

### For Writes
1. Use transactions for multi-table operations
2. Update `updated_at` timestamp on all changes
3. Set `updated_by` for audit trails
4. Handle cascade deletes carefully

---

## Security Considerations

### Multi-Tenancy
- Always enforce tenant isolation at application level
- Validate user has access to the tenant
- Never expose cross-tenant data

### Data Protection
- Encrypt sensitive fields (bank account numbers)
- Mask tax identifiers in logs
- Use HTTPS for all API calls
- Implement rate limiting

### Audit Trail
- Track all create/update/delete operations
- Store `created_by` and `updated_by` user IDs
- Maintain timestamp fields
- Log all statutory changes

---

## Troubleshooting

### Common Issues

**Issue:** Cannot create company - duplicate code
**Solution:** Check if code already exists, use unique code

**Issue:** Cannot delete company - foreign key constraint
**Solution:** Check if employees exist for company, delete employees first or use soft delete

**Issue:** Multiple primary bank accounts
**Solution:** Run cleanup query to enforce single primary

**Issue:** Missing statutory/settings data
**Solution:** Re-create default records using mutations

---

## Support

For technical issues or questions:
- **Backend API:** Development Team
- **Database:** Database Administration Team
- **Business Logic:** Product Team

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-26 | Initial documentation with all 5 tables |
| - | - | Added industryDescription field to company table |

---

## Next Steps

1. Review the complete analytics documentation
2. Understand database schema and relationships
3. Test GraphQL mutations and queries
4. Set up monitoring and alerts
5. Plan data migration strategy

---

**Last Updated:** November 26, 2025
**Maintained By:** HRMS Development Team
