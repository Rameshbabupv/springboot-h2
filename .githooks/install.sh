#!/bin/bash

# HRMS Git Hooks Installation Script
# Purpose: Set up strict branch naming and feature validation hooks
# Run once per developer setup

echo "════════════════════════════════════════════════════════════"
echo "  HRMS Git Hooks Installation"
echo "════════════════════════════════════════════════════════════"
echo ""

# Check if we're in a git repository
if [[ ! -d ".git" ]]; then
    echo "❌ Error: Not in a git repository root"
    echo "   Run this script from project root directory"
    exit 1
fi

# Set git hooks directory to .githooks
echo "📝 Configuring git to use .githooks directory..."
git config core.hooksPath .githooks

if [[ $? -eq 0 ]]; then
    echo "✅ Git hooks path configured: .githooks"
else
    echo "❌ Failed to configure git hooks path"
    exit 1
fi

# Make hooks executable
echo ""
echo "🔒 Making hooks executable..."
chmod +x .githooks/pre-push

if [[ $? -eq 0 ]]; then
    echo "✅ Hooks permissions set"
else
    echo "❌ Failed to set hook permissions"
    exit 1
fi

# Verify hooks are installed
echo ""
echo "🔍 Verifying hook installation..."
if [[ -x ".githooks/pre-push" ]]; then
    echo "✅ pre-push hook is installed and executable"
else
    echo "❌ Hook installation failed"
    exit 1
fi

# Summary
echo ""
echo "════════════════════════════════════════════════════════════"
echo "  ✅ Installation Complete!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "Installed Hooks:"
echo "  • pre-push: Validates branch names and feature checklist"
echo ""
echo "Next Steps:"
echo "  1. Create feature branch: git checkout -b feature/yourFeatureName"
echo "  2. Copy template: cp .githooks/featureBranch.template.json .featureBranch.json"
echo "  3. Fill in checklist: Edit .featureBranch.json with your details"
echo "  4. Commit checklist: git add .featureBranch.json && git commit"
echo "  5. Push: git push -u origin feature/yourFeatureName"
echo ""
echo "The pre-push hook will validate everything before allowing push."
echo ""
