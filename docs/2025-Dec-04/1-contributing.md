# Contributing to HRMS

## Developer Workflow - Strict Rules

This document outlines the mandatory workflow for all developers contributing to HRMS.

**Status**: ENFORCED via Git Hooks - No exceptions

---

## Setup (One-Time)

### Install Git Hooks

Run this command once after cloning:

```bash
cd HRMS_New_Api
bash .githooks/install.sh
```

This sets up pre-push hooks that validate your work before pushing to remote.

**What it does**:
- Validates branch naming convention
- Ensures feature checklist is filled
- Checks commit message quality
- Prevents invalid pushes (saves you from embarrassment!)

---

## Git Flow - The Rules

### 1. Branch Naming (STRICT)

**Rule**: `feature/{camelCaseDescriptiveName}`

**Pattern**: Must start with `feature/` followed by camelCase name

✅ **VALID**:
```
feature/addAttendanceModule
feature/fixEmployeeFilterBug
feature/implementGraphQLLogging
feature/refactorUserService
feature/updateDatabaseSchema
feature/addJWTAuthentication
```

❌ **INVALID**:
```
feature/add-attendance        (use camelCase, not kebab-case)
feature/ADD_ATTENDANCE        (use lowercase)
feature/attend                (too vague)
my-feature                    (missing 'feature/' prefix)
feature/work                  (meaningless)
feature/temp                  (no temporary branches)
```

**If you get it wrong**:
```bash
# Rename locally
git branch -m feature/correctName

# Update remote
git push origin :old-name origin feature/correctName
```

---

### 2. Create Feature Branch

```bash
# Step 1: Switch to develop
git checkout develop

# Step 2: Pull latest changes
git pull origin develop

# Step 3: Create your feature branch
git checkout -b feature/yourFeatureName

# IMPORTANT: Your branch name must follow the naming rule above!
```

**Do NOT**:
- Branch from main (except hotfixes)
- Use temporary names like `feature/temp` or `feature/wip`
- Use special characters or spaces

---

### 3. Pre-Work Checklist (MANDATORY)

**Before writing any code**, create the feature checklist:

```bash
# Copy template
cp .githooks/featureBranch.template.json .featureBranch.json

# Edit it
vim .featureBranch.json  # or your preferred editor
```

**What to fill in**:

```json
{
  "featureName": "Add Attendance Module",
  "branchName": "feature/addAttendanceModule",
  "createdDate": "2025-12-04",
  "developer": "Your Name",

  "what": "Implement complete attendance tracking system including check-in/check-out, shift timings, and attendance reports",

  "why": "Track employee attendance for compliance, reporting, and payroll calculations",

  "scope": {
    "components": [
      "Entity: Attendance, ShiftTiming, AttendanceLog (3 new entities)",
      "Service: AttendanceService, ShiftService",
      "Resolver: AttendanceResolver (GraphQL)",
      "Database: 3 new tables, 10 new indexes"
    ],
    "estimatedHours": 24
  },

  "acceptanceCriteria": [
    "User can record check-in/check-out time via GraphQL",
    "System calculates shift hours automatically",
    "Reports show attendance by department",
    "API enforces organizational scope filtering"
  ],

  "testing": {
    "unitTests": "yes",
    "integrationTests": "yes",
    "testsRequired": "All unit tests pass, GraphQL queries return correct data"
  },

  "dataChanges": {
    "databaseMigration": true,
    "migrationDetails": "Create attendance, shift_timing, attendance_log tables",
    "backwardCompatibility": "yes"
  },

  "notes": "Attendance calculations based on India Standard Time"
}
```

**Commit the checklist**:
```bash
git add .featureBranch.json
git commit -m "Add feature checklist: addAttendanceModule"
```

**The checklist is REQUIRED before pushing!**

---

### 4. Development

```bash
# Make your changes
vim src/main/java/com/hrms/entity/Attendance.java
vim src/main/java/com/hrms/service/AttendanceService.java
# ... etc

# Commit with meaningful messages
git add .
git commit -m "Add Attendance entity with 15 fields for shift tracking"
git commit -m "Implement AttendanceService with CRUD operations"
git commit -m "Create AttendanceResolver for GraphQL endpoint"

# Build and test locally before pushing!
mvn clean package
```

**Commit Message Rules**:
- Minimum 20 characters
- Recommended max 72 characters
- Use imperative mood: "Add feature", not "Added feature"
- Be specific: "Fix null pointer in employee filter" not "Fix bug"

---

### 5. Push to Remote

```bash
git push -u origin feature/yourFeatureName
```

**What happens**:
1. Git hook validates branch name ✓
2. Git hook checks .featureBranch.json exists and is filled ✓
3. Git hook validates commit messages ✓
4. If all pass: Your code is pushed ✓
5. If any fail: Push is blocked with clear error message ✗

**If push is rejected**:
- Read the error message carefully
- Fix the issue (usually branch name or missing checklist)
- Try push again

---

### 6. Create Pull Request

**After pushing**, create PR to `develop`:

```
Title: feature/yourFeatureName - Brief description

Description:
What: [from checklist]
Why: [from checklist]
Acceptance Criteria:
- Criterion 1
- Criterion 2

Testing: [how to verify]
```

**GitHub will automatically**:
- Run build & test checks (must pass before merge)
- Check branch protection rules

**Merge to develop only after**:
- ✅ Build succeeds
- ✅ Tests pass
- ✅ No conflicts with develop

---

### 7. Cleanup (After Merge)

```bash
# Switch back to develop
git checkout develop

# Pull merged changes
git pull origin develop

# Delete local branch
git branch -d feature/yourFeatureName

# Delete remote branch
git push origin --delete feature/yourFeatureName

# Delete checklist file from develop
rm .featureBranch.json
```

---

## Build & Test Requirements

### Before Pushing

```bash
# Build
mvn clean compile

# Run tests
mvn test

# Package
mvn clean package
```

All must pass locally before pushing.

### GitHub Actions (Automatic)

When PR is created, GitHub automatically:
1. **Compile**: `mvn clean compile` (must pass)
2. **Test**: `mvn test` (must pass)
3. **Package**: `mvn clean package` (must pass)

**If any fail**: PR cannot be merged. Fix in feature branch and push again.

---

## Branch Protection Rules

### Develop Branch
- ✅ Requires PR for all changes (no direct push)
- ✅ Requires build to pass
- ✅ Requires tests to pass
- ✅ Uses merge commits (--no-ff)

### Main Branch
- ✅ Requires PR from develop
- ✅ Requires build to pass
- ✅ Requires tests to pass
- ✅ Uses merge commits
- ✅ Administrators only

---

## Discipline & Expectations

### What Happens If You...

**Create branch with wrong name**:
```
❌ Push fails with error message
✓ Rename branch, push again
```

**Skip the checklist**:
```
❌ Push fails: Missing .featureBranch.json
✓ Copy template, fill it out, commit, push again
```

**Commit bad messages**:
```
❌ Push fails: Message too short
✓ Rewrite message: git commit --amend
```

**Skip build/tests locally**:
```
❌ GitHub rejects PR
✓ Fix locally, push again
```

### No Exceptions

These rules are **ENFORCED by Git Hooks** - they're not suggestions.

If you think a rule needs changing:
1. Discuss with team
2. Update rules in this file + hooks
3. All developers re-run `.githooks/install.sh`

---

## Troubleshooting

### "Branch name validation failed"

**Problem**: Branch doesn't match `feature/camelCaseName`

**Fix**:
```bash
git branch -m feature/correctName
git push origin :old-name origin feature/correctName
```

### "Checklist not found"

**Problem**: `.featureBranch.json` missing

**Fix**:
```bash
cp .githooks/featureBranch.template.json .featureBranch.json
# Edit the file
git add .featureBranch.json
git commit -m "Add feature checklist"
git push
```

### "Commit message too short"

**Problem**: Commit message < 20 characters

**Fix**:
```bash
git commit --amend -m "Better, longer commit message with details"
git push -f origin feature/yourFeatureName
```

### "Build failed on GitHub"

**Problem**: Tests fail in GitHub CI

**Fix**:
1. Check GitHub Actions logs
2. Reproduce locally: `mvn clean package`
3. Fix in code
4. Commit: `git commit -am "Fix failing test"`
5. Push: `git push origin feature/yourFeatureName`

---

## Questions?

Refer to:
- Architecture: `/docs/architecture.md`
- Setup: `/docs/setup.md`
- Troubleshooting: `/docs/troubleshooting.md`

---

**Last Updated**: 2025-Dec-04
**Version**: 1.0 - Strict Enforcement
**Status**: ACTIVE
