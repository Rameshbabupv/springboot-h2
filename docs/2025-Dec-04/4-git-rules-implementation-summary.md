# Git Rules Implementation - Quick Reference

Use this guide to implement same rules in other repositories.

---

## What We Implemented

1. **Git Hooks** - Pre-push validation script
2. **Checklist Template** - Feature work planning
3. **GitHub Actions** - Build/test automation
4. **Branch Protection** - GitHub rules enforcement

---

## For Other Repos - Implementation Steps

### Step 1: Create Git Hooks Directory

```bash
mkdir -p .githooks
```

### Step 2: Create Pre-Push Hook

**File**: `.githooks/pre-push`

```bash
#!/bin/bash
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Rule 1: Branch name validation
if [[ "$CURRENT_BRANCH" == "feature/"* ]]; then
    PATTERN="^feature/[a-z]+([a-zA-Z0-9]*)?$"
    if [[ ! $CURRENT_BRANCH =~ $PATTERN ]]; then
        echo "❌ Invalid branch name: $CURRENT_BRANCH"
        echo "   Required: feature/camelCaseName"
        exit 1
    fi
fi

# Rule 2: Checklist file validation
if [[ "$CURRENT_BRANCH" == "feature/"* ]]; then
    if [[ ! -f ".featureBranch.json" ]]; then
        echo "❌ Missing .featureBranch.json checklist"
        exit 1
    fi
fi

exit 0
```

**Make executable**:
```bash
chmod +x .githooks/pre-push
```

**Configure git**:
```bash
git config core.hooksPath .githooks
```

### Step 3: Create Checklist Template

**File**: `.githooks/featureBranch.template.json`

```json
{
  "featureName": "FEATURE_NAME",
  "developer": "YOUR_NAME",
  "what": "DESCRIBE_WHAT_YOU_ARE_BUILDING",
  "why": "DESCRIBE_WHY_THIS_IS_NEEDED",
  "acceptanceCriteria": [
    "CRITERION_1",
    "CRITERION_2"
  ],
  "testing": "DESCRIBE_TESTING_APPROACH"
}
```

### Step 4: Create Install Script

**File**: `.githooks/install.sh`

```bash
#!/bin/bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-push
echo "✅ Git hooks installed"
```

**Make executable**:
```bash
chmod +x .githooks/install.sh
```

### Step 5: Create GitHub Actions Workflow

**File**: `.github/workflows/build-and-test.yml`

```yaml
name: Build & Test
on:
  push:
    branches: [ develop, main ]
  pull_request:
    branches: [ develop, main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    - run: mvn clean compile
    - run: mvn test
    - run: mvn clean package -DskipTests
```

### Step 6: Commit & Push

```bash
git add .githooks/ .github/workflows/
git commit -m "Add git hooks and CI/CD pipeline"
git push origin develop
```

### Step 7: GitHub Branch Protection (Manual)

Go to: Settings → Branches → Add Rule

**For `develop` branch**:
- ✅ Require pull request
- ✅ Require status checks: build-and-test
- ✅ Require branches up-to-date
- ✅ Require merge commits

**For `main` branch**: Same as develop

---

## That's It!

**Developers do**:
```bash
bash .githooks/install.sh
```

**Then follow workflow**:
```bash
git checkout -b feature/myFeatureName
cp .githooks/featureBranch.template.json .featureBranch.json
# Edit checklist...
git add . && git commit
mvn clean package
git push origin feature/myFeatureName
# → Hook validates → Pushed
# Create PR → GitHub Actions runs → Merge when pass
```

---

## Files Needed (Copy & Paste Ready)

### .githooks/pre-push
```bash
#!/bin/bash
BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Validate branch name: feature/camelCaseName
if [[ "$BRANCH" == "feature/"* ]]; then
    if [[ ! $BRANCH =~ ^feature/[a-z]+([a-zA-Z0-9]*)?$ ]]; then
        echo "❌ Invalid: feature/camelCaseName required"
        exit 1
    fi

    # Validate checklist exists
    if [[ ! -f ".featureBranch.json" ]]; then
        echo "❌ Missing: .featureBranch.json"
        exit 1
    fi
fi

exit 0
```

### .githooks/featureBranch.template.json
```json
{
  "featureName": "NAME",
  "developer": "YOUR_NAME",
  "what": "WHAT_ARE_YOU_BUILDING",
  "why": "WHY_IS_IT_NEEDED",
  "acceptanceCriteria": ["CRITERION_1", "CRITERION_2"],
  "testing": "HOW_TO_TEST"
}
```

### .githooks/install.sh
```bash
#!/bin/bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-push
echo "✅ Setup complete"
```

### .github/workflows/build-and-test.yml
```yaml
name: Build & Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    - run: mvn clean compile
    - run: mvn test
    - run: mvn clean package -DskipTests
```

---

## Branch Protection Setup (GitHub UI)

```
Settings → Branches → Add rule

Branch name pattern: develop (or main)

✅ Require pull request reviews: OFF (optional)
✅ Require status checks to pass: ON
   - build-and-test (GitHub Actions)
✅ Require branches to be up to date: ON
✅ Require merge commits: ON
```

---

## That's All!

One developer setup command:
```bash
bash .githooks/install.sh
```

Rest is automatic via git hooks + GitHub Actions + branch rules.

---

**Status**: Ready to copy to any repo
**Time to implement**: 10 minutes
**Maintenance**: Zero
