#!/bin/bash
# ============================================
#   GitHub Repository Setup Script
#   Run this once to initialize and push to GitHub
# ============================================

set -e

echo "============================================"
echo "  GitHub Repository Setup"
echo "============================================"

# Check git
if ! command -v git &> /dev/null; then
    echo "[ERROR] Git not installed!"
    exit 1
fi

# Initialize git if needed
if [ ! -d .git ]; then
    echo "[1/4] Initializing git repository..."
    git init
    git branch -M main
else
    echo "[1/4] Git repository already exists"
fi

# Create .gitignore if not exists
echo "[2/4] Checking .gitignore..."
if ! grep -q ".env" .gitignore 2>/dev/null; then
    echo ".env" >> .gitignore
    echo "ssl/" >> .gitignore
    echo "backups/" >> .gitignore
    echo "logs/" >> .gitignore
    echo "node_modules/" >> .gitignore
    echo "target/" >> .gitignore
    echo "*.class" >> .gitignore
    echo ".idea/" >> .gitignore
    echo ".vscode/" >> .gitignore
fi

# Commit
echo "[3/4] Committing files..."
git add .
git commit -m "Initial commit: JXC Management System"

echo "[4/4] Ready to push!"
echo ""
echo "Next steps:"
echo "1. Create a repository on GitHub: https://github.com/new"
echo "2. Run these commands:"
echo ""
echo "   git remote add origin https://github.com/YOUR_USERNAME/jxc-system.git"
echo "   git push -u origin main"
echo ""
echo "3. Then go to Railway.app to deploy (see DEPLOY.md)"
