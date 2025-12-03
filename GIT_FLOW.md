# Git Flow Strategy - HRMS Project

## Overview
This project follows a structured Git Flow approach to manage development, releases, and hotfixes. This document outlines the branching strategy and workflows.

## Branch Structure

### Main Branches
- **`main`**: Production-ready code
  - Protected branch (requires pull requests)
  - Merge only from release/hotfix branches
  - Tags mark releases (v1.0, v1.1, etc.)

- **`develop`**: Integration branch for features
  - Base branch for feature development
  - Merge only from feature branches
  - Should be in a stable, deployable state

### Supporting Branches
- **Feature branches** (`feature/*`): For new features
  - Created from: `develop`
  - Merged back to: `develop`
  - Naming: `feature/feature-name` or `feature/JIRA-123`

- **Release branches** (`release/*`): For release preparation
  - Created from: `develop`
  - Merged back to: `develop` and `main`
  - Naming: `release/v1.0` or `release/1.0.0`

- **Hotfix branches** (`hotfix/*`): For production fixes
  - Created from: `main`
  - Merged back to: `main` and `develop`
  - Naming: `hotfix/bug-name` or `hotfix/JIRA-456`

## Merge Strategy

- **Strategy**: Merge commits (no fast-forward)
- **Configuration**: `pull.ff=false`
- **Merge commits** preserve branch history and context
- **All merges** should be done via Pull Requests (PR)

## Workflow Examples

### Starting a Feature

```bash
# Update develop branch
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/new-feature

# Make changes, commit, and push
git add .
git commit -m "Add new feature"
git push -u origin feature/new-feature

# Create Pull Request on GitHub
# After review and approval, merge with "Create merge commit"
```

### Creating a Release

```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.0

# Update version numbers and prepare release
# Make any final adjustments
git add .
git commit -m "Prepare release v1.0"
git push -u origin release/v1.0

# Create Pull Request to main
# Merge with "Create merge commit"

# After merging to main, merge back to develop
git checkout develop
git pull origin develop
git merge --no-ff release/v1.0
git push origin develop

# Delete release branch
git branch -d release/v1.0
git push origin --delete release/v1.0
```

### Creating a Hotfix

```bash
# Create hotfix branch from main
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug

# Fix the issue and test
git add .
git commit -m "Fix critical bug"
git push -u origin hotfix/critical-bug

# Create Pull Request to main
# Merge with "Create merge commit"

# After merging to main, merge back to develop
git checkout develop
git pull origin develop
git merge --no-ff hotfix/critical-bug
git push origin develop

# Delete hotfix branch
git branch -d hotfix/critical-bug
git push origin --delete hotfix/critical-bug
```

## Branch Protection Rules

### Main Branch
- ✅ Require pull request reviews before merging
- ✅ Require status checks to pass before merging
- ✅ Require merge commit strategy
- ✅ Dismiss stale pull request approvals when new commits are pushed
- ✅ Include administrators in restrictions

### Develop Branch
- ✅ Require pull request reviews before merging
- ✅ Require at least 1 approval before merging
- ✅ Require merge commit strategy
- ✅ Require status checks to pass before merging

### Feature Branches
- No specific protection rules required
- Should follow naming convention: `feature/*`

## Commit Message Guidelines

Use clear, descriptive commit messages:
- Use imperative mood: "Add feature" not "Added feature"
- Keep first line under 50 characters
- Provide detailed explanation in body if needed
- Reference JIRA tickets: "JIRA-123: Add new feature"

Example:
```
Add authentication module

- Implement JWT token validation
- Create login endpoint
- Add user session management

Fixes JIRA-456
```

## Tag Conventions

For releases, use semantic versioning tags on main branch:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

Format: `v{MAJOR}.{MINOR}.{PATCH}`

## Code Review Process

1. **Create** feature branch and push changes
2. **Open** Pull Request with clear description
3. **Assign** reviewers (minimum 1 approval required)
4. **Address** feedback and make requested changes
5. **Approve** and merge with "Create merge commit"
6. **Verify** changes are correctly merged to develop

## FAQ

**Q: Can I push directly to main or develop?**
A: No, all changes must go through Pull Requests.

**Q: What if I accidentally created a branch from the wrong parent?**
A: Create a new branch from the correct parent and recreate your commits.

**Q: Should I merge or rebase?**
A: Always merge with merge commits (`--no-ff`) to preserve history.

**Q: How do I keep my feature branch updated with develop?**
A: Use `git merge develop` (not rebase) to bring in the latest changes.

**Q: Can I work on multiple features at once?**
A: Yes, create separate feature branches for each feature.
