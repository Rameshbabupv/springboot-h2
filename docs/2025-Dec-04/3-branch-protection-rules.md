# Branch Protection Rules

## Overview

Branch protection rules ensure code quality, enforced automated checks, and prevent accidental pushes to critical branches.

**Status**: REQUIRED for develop and main branches

---

## Configure Protect Rules (GitHub Admin)

### Access GitHub Settings

1. Go to repository: `https://github.com/YOUR_ORG/HRMS_New_Api`
2. Click **Settings** (top right)
3. Navigate to **Branches** (left sidebar)
4. Click **Add rule** under "Branch protection rules"

---

## Develop Branch Rules

### Step 1: Create Rule

**Branch name pattern**: `develop`

### Step 2: Configure Requirements

✅ **Require pull request reviews before merging**
- Required number of dismissals: **0**
- Require code review from code owners: **No** (as per user requirement)

✅ **Require status checks to pass before merging**
- Status checks that must pass:
  - `build-and-test / build-and-test`
  - `build-and-test / schema-validation`
  - `build-and-test / ci-status`

✅ **Require branches to be up to date before merging**
- ✓ Selected (prevents stale branches)

✅ **Require merge commits**
- ✓ Selected (enforces `--no-ff` merge strategy)

❌ **Do NOT require code review approval** (as per user requirement)

### Step 3: Additional Settings

❌ **Dismiss stale pull request approvals**: Not applicable (no reviews required)

❌ **Require approval of the latest commit review**: Not applicable

⚠️ **Require conversation resolution before merging**: **No**
- Users can merge even with unresolved conversations

### Step 4: Exceptions & Admin Override

**Bypass actors**:
- Leave empty (no one can bypass these rules)

Or if needed:
- Add specific admin accounts who can force push
- NOT recommended for develop - keeps it safe

### Step 5: Save Rule

Click **Create** to save develop branch protection rule.

---

## Main Branch Rules

### Step 1: Create Rule

**Branch name pattern**: `main`

### Step 2: Configure Requirements

✅ **Require pull request reviews before merging**
- Required number of dismissals: **0** (can relax if desired)
- Require code review from code owners: **No**

✅ **Require status checks to pass before merging**
- Status checks that must pass:
  - `build-and-test / build-and-test`
  - `build-and-test / schema-validation`
  - `build-and-test / ci-status`

✅ **Require branches to be up to date before merging**
- ✓ Selected

✅ **Require merge commits**
- ✓ Selected (enforces `--no-ff`)

✅ **Include administrators** (RECOMMENDED for main)
- ✓ Selected (admins can't bypass rules either)

### Step 3: Restrictions on who can push

**Restrict who can push to matching branches**:
- Allow admins only
- Or specify specific users

**Effect**: Only designated people can force push to main (for emergencies)

### Step 4: Save Rule

Click **Create** to save main branch protection rule.

---

## How Rules Work

### Scenario 1: Developer Creates Feature Branch

```
1. Developer creates: feature/addAttendanceModule
2. Git hook validates branch name locally ✓
3. Developer fills .featureBranch.json ✓
4. Developer commits code and tests locally
5. Developer pushes: git push -u origin feature/addAttendanceModule ✓
6. Pre-push hook validates:
   - Branch name ✓
   - Checklist ✓
   - Commit messages ✓
7. Code is pushed to remote
```

### Scenario 2: Developer Creates Pull Request to Develop

```
1. Developer creates PR: feature/addAttendanceModule → develop
2. GitHub automatically:
   - Runs build-and-test workflow
   - Compiles code
   - Runs unit tests
   - Creates JAR package
   - Validates schema
3. Status checks appear in PR:
   ✅ build-and-test / build-and-test
   ✅ build-and-test / schema-validation
   ✅ build-and-test / ci-status
4. When all pass: "Merge pull request" button becomes available
5. Click merge → Code is merged to develop
6. Develop branch is automatically updated
```

### Scenario 3: Developer Tries to Push Directly to Develop

```
1. Developer tries: git push origin develop
2. GitHub rejects: "Protected branch"
3. Developer must create PR instead
4. Forced to follow workflow ✓
```

### Scenario 4: Code Fails Checks

```
1. Developer creates PR with failing tests
2. GitHub runs checks and fails:
   ❌ Tests failed in test suite
3. "Merge pull request" button is DISABLED
4. Developer must:
   - Fix code locally
   - git push again (new commits added to PR)
   - GitHub re-runs checks
5. Once all pass → Can merge
```

---

## Workflow Diagram

```
┌─────────────────────────────────────────┐
│  Developer creates feature/addFeature   │
└──────────────────┬──────────────────────┘
                   │
                   ▼
        ┌─────────────────────┐
        │  Git Hook Validates │
        │ • Branch name ✓     │
        │ • Checklist ✓       │
        │ • Messages ✓        │
        └────────┬────────────┘
                 │
         ✓ PASS / ❌ FAIL
         │         │
         ▼         └──→ REJECTED
         │              (fix & retry)
         │
    ┌────┴──────────────────┐
    │ Push to remote         │
    │ git push -u origin ... │
    └────────┬───────────────┘
             │
             ▼
    ┌─────────────────────────┐
    │  Create Pull Request    │
    │  feature → develop      │
    └────────┬────────────────┘
             │
             ▼
    ┌──────────────────────────┐
    │  GitHub Actions: CI/CD   │
    │  • Build: mvn clean      │
    │  • Test: mvn test        │
    │  • Package: mvn package  │
    │  • Schema validation     │
    └────────┬─────────────────┘
             │
    ✓ ALL PASS / ❌ FAIL
    │             │
    ▼             └──→ STATUS: FAILED
    │                 (Review logs, fix, push)
    │
    ┌─────────────────────────┐
    │ Status Checks Passed    │
    │ ✅ build-and-test       │
    │ ✅ schema-validation    │
    │ ✅ ci-status            │
    └────────┬────────────────┘
             │
             ▼
    ┌──────────────────────────┐
    │ Merge Pull Request      │
    │ (Admin/Dev can merge)   │
    └────────┬─────────────────┘
             │
             ▼
    ┌──────────────────────────┐
    │ Develop Branch Updated  │
    │ All tests passed ✅     │
    │ Code deployed safely    │
    └──────────────────────────┘
```

---

## Enforcement Summary

| Rule | Develop | Main | Enforced By |
|------|---------|------|-------------|
| Merge commits only | ✅ | ✅ | GitHub |
| Build must pass | ✅ | ✅ | GitHub Actions |
| Tests must pass | ✅ | ✅ | GitHub Actions |
| Branch up-to-date | ✅ | ✅ | GitHub |
| PR required | ✅ | ✅ | GitHub |
| Branch name validation | ✅ | N/A | Git hooks |
| Checklist required | ✅ | N/A | Git hooks |

---

## Troubleshooting

### "Cannot merge: Status check failed"

**Problem**: Build or tests failed in GitHub Actions

**Solution**:
1. Click on the failed check to see logs
2. Identify what failed (compile error, test failure, etc.)
3. Fix locally: `mvn clean package`
4. Commit and push: New push triggers check again
5. Wait for checks to re-run and pass

### "Merge button is disabled"

**Problem**: One of the required status checks hasn't completed yet

**Solution**:
- Wait for GitHub Actions to finish running
- Can see progress in "Checks" tab of PR
- Takes 2-5 minutes typically

### "Your branch is out of date"

**Problem**: Develop has been updated since you created your PR

**Solution**:
```bash
git fetch origin
git rebase origin/develop
git push --force-with-lease origin feature/yourFeatureName
```

Or use GitHub's "Update branch" button in the PR.

### "Cannot push to develop directly"

**Problem**: Trying to push directly instead of via PR

**Solution**:
```bash
# Don't do this:
git push origin develop

# Instead, create PR:
git push origin feature/yourFeatureName
# Then create PR on GitHub
```

---

## Emergency: Override Rules

**If production is down** and you need to force push:

```bash
# Only repo admins can do this
git push --force-with-lease origin main
```

This bypasses branch protection **but**:
1. Creates audit trail (GitHub logs it)
2. Should only happen in emergencies
3. Document why: Create issue explaining incident
4. Follow up after incident with proper fix

**Best practice**: Even in emergencies, use `--force-with-lease` not `--force` to avoid overwriting others' work.

---

**Configuration Last Updated**: 2025-Dec-04
**Rule Version**: 1.0 - Strict Enforcement
**Status**: ACTIVE
