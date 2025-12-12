# Architecture Decision Records (ADR)

This directory contains all Architecture Decision Records for the HRMS SaaS system. Each ADR documents important architectural decisions, their context, consequences, and alternatives considered.

## ADR Format

Each ADR follows the standard format:
- **Title**: Clear, concise decision statement
- **Status**: PROPOSED, ACCEPTED, DEPRECATED, SUPERSEDED
- **Context**: Problem statement and background
- **Decision**: The chosen approach
- **Consequences**: Positive and negative outcomes
- **Alternatives**: Other options considered and why they were rejected

## Active ADRs

### [ADR-001: Biometric Sync Integration Architecture](./ADR-001-biometric-sync-architecture.md)

**Status**: ACCEPTED (with recommended improvements)
**Date**: 2025-12-06

**Summary**: Defines the architecture for syncing biometric punch data from Supabase cloud database into HRMS.

**Current State**: In-memory processing with direct database inserts.
**Recommendation**: Implement multi-stage ETL with staging table for reliability, auditability, and failure recovery.

**Key Decisions**:
- Staging table (`biometric_sync_staging`) to persist raw records
- Batch processing (1000 records/transaction) with checkpoints
- 30-day retention policy with automatic purging
- Monitoring and metrics via GraphQL queries

**Components Affected**:
- `BiometricSyncService.java`
- `BiometricSyncServiceImpl.java`
- Database schema (new staging tables)
- `BiometricSyncResolver.java` (monitoring queries)

**Phases**:
1. Phase 1 (Sprint 1): Staging infrastructure
2. Phase 2 (Sprint 1-2): Service refactoring
3. Phase 3 (Sprint 2): Monitoring & operations
4. Phase 4 (Sprint 2-3): Testing & validation
5. Phase 5 (Future): Async processing

---

## Proposed ADRs (Not Yet Approved)

None currently.

---

## Deprecated ADRs

None currently.

---

## Related Documentation

- [Architecture Overview](../architecture.md) - System-level architecture
- [Database Schema](../database.md) - Database design and relationships
- [API Documentation](../api.md) - GraphQL API endpoints
- [Attendance Module Planning](../2025-Dec-03/3-attendance-module-planning.md) - Module context

---

## Decision Log

| ADR | Decision | Date | Status |
|-----|----------|------|--------|
| ADR-001 | Biometric Sync staging architecture | 2025-12-06 | ACCEPTED |

---

## How to Use This Directory

### For Developers
1. Review relevant ADRs before implementing features
2. Check ADR status to understand current architectural constraints
3. Reference ADR decisions in code comments when applicable

### For Architects
1. Review ADRs before major refactoring
2. Update ADR status as decisions are implemented
3. Create new ADRs for significant architectural changes

### For DevOps/Database Teams
1. Check ADRs for infrastructure implications
2. Review configuration and monitoring requirements
3. Plan for schema changes and migrations

---

## Creating a New ADR

### Naming Convention
Use sequential numbering: `ADR-###-short-title.md`

Example: `ADR-002-api-versioning-strategy.md`

### Template
```markdown
# ADR-###: [Title]

**Status**: PROPOSED | ACCEPTED | DEPRECATED | SUPERSEDED
**Date**: YYYY-MM-DD
**Decision Maker**: [Role/Team]
**Stakeholders**: [List of stakeholders]

## Context
[Background and problem statement]

## Decision
[The chosen approach]

## Consequences

### Positive
- [Benefit 1]

### Negative
- [Drawback 1]

### Risks
- [Risk 1]

## Alternatives Considered

### Alternative 1
**Pros**: [...]
**Cons**: [...]
**Verdict**: REJECTED

## Implementation Plan
- [ ] Phase 1
- [ ] Phase 2

## References
- [Link 1]

## Approval & Sign-off
- Approver 1: PENDING
```

### Submission Process
1. Create ADR in draft status
2. Share with stakeholders for review (1-2 week review period)
3. Incorporate feedback
4. Update status to ACCEPTED or REJECTED
5. Commit to repository with ADR branch

---

## Review Schedule

- **Initial Review**: Monthly (first Monday of month)
- **Status Updates**: Quarterly (per phase completion)
- **Annual Audit**: End of calendar year

---

**Last Updated**: 2025-12-06
**Maintained By**: Architecture Team
**Next Review**: 2025-01-06
