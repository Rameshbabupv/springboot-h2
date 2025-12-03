# HRMS Known Quirks & Oddities

## "It Works™" Features

### 1. The 78-Field Employee Entity
**What**: Employee table has 78 columns
**Why**: Indian statutory compliance requirements are... extensive
**Side effect**: Your IDE autocomplete now has commitment issues
**How to cope**: Coffee. Lots of coffee.

### 2. Organizational Scope Magic
**What**: 9-dimensional security enforcement
**Why**: Multi-tenant SaaS meets paranoia
**Side effect**: Users think data disappeared (it didn't, they just don't have access)
**How to cope**: Check `user_organizational_scope` table before blaming code

### 3. GraphQL Returns Empty Array
**What**: Query returns `[]` when you expect data
**Why**: Either (a) no data, (b) user scope blocks access, or (c) you broke something
**Diagnosis**: Check logs. Then database. Then logs again.
**How to cope**: Existential crisis is optional

### 4. JWT Token Expired
**What**: Token works, then suddenly... doesn't
**Why**: 8 hours is what we decided was fair
**Side effect**: Users hate you during long standup meetings
**How to cope**: Refresh token exists for a reason (use it)

### 5. Database Port Not Listening
**What**: Can't connect to PostgreSQL
**Why**: It's not running, or your credentials are wrong, or the universe hates you
**How to cope**: `psql -U postgres` then drink tea while thinking about your life choices

## "Feature Not Bug" Incidents

- Multi-tenancy prevents cross-tenant data access ✅ (intentional)
- Super Admin role has no restrictions ✅ (intentional & necessary)
- Pagination defaults to 20 items ✅ (intentional & merciful to the database)
- Password expires every 90 days ✅ (blame security team, not us)

## The "I Swear This Is Not a Bug" Hall of Fame

| Issue | Status | Explanation |
|-------|--------|-------------|
| "Why is my data gone?" | FEATURE | Organizational scope filtering. User not authorized. |
| "GraphQL query is slow" | FEATURE | 78 fields is a lot. Add pagination. Use filters. |
| "Account locked after 5 logins" | FEATURE | Security. Failed login counting. Works as designed. |
| "Why do I need a token?" | FEATURE | JWT security. This is how modern APIs work. |
| "Database taking forever" | MAYBE A BUG | Check indexes. Check queries. Check coffee levels. |

---

*Remember: It's not a bug, it's an undocumented feature. (Until now.)*
