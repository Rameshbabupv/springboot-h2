# HRMS Developer Workflow - Complete System

## Overview

A complete, enforced development workflow for HRMS with strict rules, automated validation, and CI/CD pipeline. No exceptions via technical enforcement.

**Status**: ✅ FULLY IMPLEMENTED & ACTIVE

---

## What's Included

### 1. Git Hooks (Local Enforcement)

**Files**:
- `.githooks/pre-push` - Bash script that runs before every push
- `.githooks/featureBranch.template.json` - Checklist template
- `.githooks/install.sh` - Setup script for developers

**What it validates**:
- ✅ Branch name follows `feature/camelCaseName` pattern
- ✅ `.featureBranch.json` checklist exists and is properly filled
- ✅ Commit messages are meaningful (min 20 chars)
- ✅ Blocks push if any validation fails

**How developers use it**:
```bash
# One-time setup (run this first)
bash .githooks/install.sh

# Now, any git push will validate automatically
git push origin feature/yourFeatureName
# → Hook validates → Push succeeds or fails with clear error
```

---

### 2. GitHub Actions CI/CD Pipeline

**File**: `.github/workflows/build-and-test.yml`

**What it does**:
- Runs on: Every PR to `develop` and `main`
- Builds code: `mvn clean compile`
- Runs tests: `mvn test`
- Creates package: `mvn clean package`
- Validates schema: Database integrity checks

**Status checks**:
- `build-and-test / build-and-test` - Build, test, package
- `build-and-test / schema-validation` - Database schema valid
- `build-and-test / ci-status` - Overall status summary

**Enforcement**:
- PR cannot be merged if any check fails
- Status checks visible in GitHub PR interface
- Developers see exact build/test failures

---

### 3. Branch Protection Rules

**Applies to**: `develop` and `main` branches

**Enforced by GitHub settings**:
- ✅ Requires PR for all changes (no direct push)
- ✅ Requires build to pass (GitHub Actions)
- ✅ Requires tests to pass (GitHub Actions)
- ✅ Requires branches up-to-date (no stale PRs)
- ✅ Merge commits enforced (`--no-ff`)
- ❌ Code review approval NOT required (team choice)

**How to configure**: See `docs/branchProtectionRules.md`

---

### 4. Contributing Guidelines

**File**: `CONTRIBUTING.md` (root level)

**Includes**:
- Complete workflow step-by-step
- Branch naming rules with examples
- Feature checklist requirements
- Commit message guidelines
- Build & test requirements
- Troubleshooting guide

---

## Developer Workflow (Start to Finish)

### Setup (One-Time)

```bash
# Clone project
git clone https://github.com/yourorg/HRMS_New_Api.git
cd HRMS_New_Api

# Install git hooks
bash .githooks/install.sh

# Verify installation
echo "Git hooks installed!" ✅
```

### Creating a Feature (New Work)

```bash
# Step 1: Create feature branch
git checkout develop
git pull origin develop
git checkout -b feature/yourFeatureName

# Step 2: Create checklist
cp .githooks/featureBranch.template.json .featureBranch.json

# Step 3: Fill checklist (IMPORTANT!)
vim .featureBranch.json
# Edit: what, why, scope, acceptance criteria, testing, etc.

# Step 4: Commit checklist
git add .featureBranch.json
git commit -m "Add feature checklist: yourFeatureName"

# Step 5: Write code & commit
vim src/main/java/com/hrms/...
git add .
git commit -m "Add Attendance entity with 15 fields"
git commit -m "Implement AttendanceService CRUD"

# Step 6: Build & test locally
mvn clean package
# All tests must pass locally before pushing!

# Step 7: Push to remote
git push -u origin feature/yourFeatureName

# What happens:
# → Pre-push hook validates branch name ✓
# → Pre-push hook checks checklist exists & filled ✓
# → Pre-push hook validates commit messages ✓
# → Code is pushed to remote ✓
```

### Creating Pull Request

```bash
# Go to GitHub
# Click "Create Pull Request" (branch page)

# Fill PR details:
Title: feature/yourFeatureName - Brief description
Description:
  What: [from checklist]
  Why: [from checklist]
  Acceptance Criteria:
  - Criterion 1
  - Criterion 2

# Click "Create Pull Request"

# GitHub automatically:
# → Runs build-and-test workflow
# → Compiles, tests, packages
# → Validates schema
# → Shows status checks in PR
```

### Merging to Develop

```bash
# Wait for status checks to pass (2-5 minutes)
# ✅ build-and-test / build-and-test - PASSED
# ✅ build-and-test / schema-validation - PASSED
# ✅ build-and-test / ci-status - PASSED

# When all green:
# Click "Merge pull request" → Code goes to develop

# Cleanup:
git checkout develop
git pull origin develop
git branch -d feature/yourFeatureName
git push origin --delete feature/yourFeatureName
rm .featureBranch.json
```

---

## Enforcement Summary

| Check | Enforced By | Failure Result |
|-------|------------|----------------|
| Branch name format | Git hook (pre-push) | Push blocked |
| Checklist required | Git hook (pre-push) | Push blocked |
| Checklist filled | Git hook (pre-push) | Push blocked |
| Commit message quality | Git hook (pre-push) | Push blocked |
| **Build succeeds** | GitHub Actions | **Merge blocked** |
| **Tests pass** | GitHub Actions | **Merge blocked** |
| **Schema valid** | GitHub Actions | **Merge blocked** |
| Merge commits | GitHub branch rules | **Merge blocked** |
| PR required | GitHub branch rules | **Push blocked** |

---

## What Happens If...

### Developer creates wrong branch name

```bash
$ git push origin my-feature
❌ Pre-push hook rejects:
   "INVALID branch name!"
   Required format: feature/camelCaseName
```

**Fix**:
```bash
git branch -m feature/myFeature
git push origin :my-feature origin feature/myFeature
```

### Developer skips checklist

```bash
$ git push origin feature/myFeature
❌ Pre-push hook rejects:
   "MISSING feature checklist!"
   Create: cp .githooks/featureBranch.template.json .featureBranch.json
```

**Fix**:
```bash
cp .githooks/featureBranch.template.json .featureBranch.json
# Edit file...
git add .featureBranch.json
git commit -m "Add feature checklist"
git push origin feature/myFeature
```

### Tests fail in GitHub Actions

```bash
PR Status: ❌ build-and-test failed
           Tests failed: 3 failures, 0 passes

Merge button: DISABLED (can't merge)
```

**Fix**:
```bash
# Fix code locally
mvn test  # see failures
# Fix the code...
git add .
git commit -m "Fix failing test X"
git push origin feature/myFeature
# GitHub automatically re-runs checks
```

### Direct push to develop attempted

```bash
$ git push origin develop
❌ GitHub rejects:
   "Branch is protected. Required status checks must pass."
   "Must use pull request."
```

**Fix**: Create PR instead (can't bypass this)

---

## Useful Commands

```bash
# Install hooks (one-time)
bash .githooks/install.sh

# Create feature branch
git checkout -b feature/myFeatureName

# Build locally
mvn clean package

# Commit with meaningful message
git commit -m "Add Attendance entity with timestamp tracking"

# Push (hook validates)
git push -u origin feature/myFeatureName

# View recent commits
git log --oneline -10

# Check git hook status
git config core.hooksPath

# View checklist
cat .featureBranch.json

# Delete feature branch (after merge)
git branch -d feature/myFeatureName
git push origin --delete feature/myFeatureName
```

---

## Files & Locations

```
Project Root
├── .githooks/
│   ├── pre-push (executable, validates everything)
│   ├── install.sh (executable, sets up hooks)
│   └── featureBranch.template.json (checklist template)
├── .github/
│   └── workflows/
│       └── build-and-test.yml (GitHub Actions CI/CD)
├── CONTRIBUTING.md (complete developer guide)
├── docs/
│   └── branchProtectionRules.md (GitHub setup guide)
└── src/main/... (your code)
```

---

## Philosophy

**"Strict but fair"**

- Rules are enforced **technically**, not culturally
- Developers can't accidentally violate rules
- Clear error messages guide them
- No human policing needed
- Everyone follows the same process
- Consistency guaranteed

**Why strict?**

- Prevents bad commit messages cluttering history
- Prevents accidental pushes to protected branches
- Ensures all code is tested before merging
- Ensures all commits are meaningful
- Scales with team growth (works for 5 or 50 devs)

---

## Team Adoption Checklist

**For all developers**:
- [ ] Clone project
- [ ] Run `bash .githooks/install.sh`
- [ ] Read `CONTRIBUTING.md`
- [ ] Create first feature branch using workflow
- [ ] Fill checklist properly
- [ ] Push and create PR
- [ ] Watch GitHub Actions run tests
- [ ] Merge when all checks pass

**For admins**:
- [ ] Configure branch protection rules (see `docs/branchProtectionRules.md`)
- [ ] Set up GitHub Actions (already included, enable in settings)
- [ ] Add team members as contributors
- [ ] Review first few PRs to ensure workflow understood

---

## Troubleshooting

**Common Issues & Solutions**: See `CONTRIBUTING.md` troubleshooting section

**Questions?**: Refer to
- `CONTRIBUTING.md` - Workflow & rules
- `docs/branchProtectionRules.md` - GitHub setup
- `.githooks/pre-push` - What validations run

---

## Summary

✅ **Complete System Implemented**
- Git hooks for local validation
- GitHub Actions for CI/CD
- Branch protection rules
- Comprehensive documentation
- One-time setup for developers

✅ **What's Enforced**
- Branch naming convention
- Feature checklist requirement
- Commit message quality
- Build & test success
- Merge commit strategy

✅ **No Exceptions**
- Rules enforced technically (can't bypass)
- Clear error messages guide developers
- Consistent across entire team

✅ **Ready for Team**
- Developers run: `bash .githooks/install.sh`
- Admins configure: GitHub branch protection rules
- Team follows: `CONTRIBUTING.md` workflow

---

**Status**: 🟢 ACTIVE & OPERATIONAL
**Version**: 1.0
**Last Updated**: 2025-Dec-04
**Enforcement Level**: STRICT (No Exceptions)
