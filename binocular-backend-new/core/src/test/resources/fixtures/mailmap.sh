#!/usr/bin/env bash
set -euo pipefail

# Force Git to use UTC for all timestamps
export TZ=UTC

# Usage check
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <directory>"
  exit 1
fi

REPO_DIR="$1"
mkdir -p "$REPO_DIR"
cd "$REPO_DIR"

# Initialize on master branch explicitly
git init -q -b master

###############################################################################
# Helper to commit with fixed author/committer data
###############################################################################
git_commit() {
  local msg="$1"; shift
  local date="$1"; shift
  local name="$1"; shift
  local email="$1"
  GIT_AUTHOR_DATE="$date"   GIT_COMMITTER_DATE="$date"   \
  GIT_AUTHOR_NAME="$name"   GIT_AUTHOR_EMAIL="$email"   \
  GIT_COMMITTER_NAME="$name" GIT_COMMITTER_EMAIL="$email" \
    git commit -m "$msg" -q
}

###############################################################################
# Create .mailmap file first
# Format: Proper Name <proper@email.com> Old Name <old@email.com>
###############################################################################
cat > .mailmap << 'EOF'
Alice Developer <alice@company.com> Alice <alice@example.com>
Alice Developer <alice@company.com> alice <alice@home.local>
Bob Builder <bob@company.com> Bob <bob@example.com>
Bob Builder <bob@company.com> Bobby <bobby@personal.net>
Carol Coder <carol@company.com> Carol <carol@example.com>
Carol Coder <carol@company.com> C. Coder <ccoder@oldcompany.org>
Dave DevOps <dave@company.com> Dave <dave@example.com>
EOF

git add .mailmap
git_commit "Add .mailmap for author normalization" \
           "2023-01-01T12:00:00+00:00" \
           "Admin" "admin@company.com"

###############################################################################
# Commits with old email addresses (before mailmap correction)
###############################################################################

# 1: Commit by Alice with old email
echo "Hello from Alice" > file1.txt
git add file1.txt
git_commit "Initial commit by Alice" \
           "2023-01-01T12:16:00+00:00" \
           "Alice" "alice@example.com"

# 2: Commit by Bob with old email
echo "Addition by Bob" >> file1.txt
git add file1.txt
git_commit "Bob's contribution" \
           "2023-01-01T13:00:00+00:00" \
           "Bob" "bob@example.com"

# 3: Commit by Carol with old email
echo "Carol's work" > file2.txt
git add file2.txt
git_commit "Carol adds file2" \
           "2023-01-01T14:00:00+00:00" \
           "Carol" "carol@example.com"

# 4: Commit by Alice with different old email (home)
echo "Alice at home" >> file1.txt
git add file1.txt
git_commit "Alice works from home" \
           "2023-01-01T15:00:00+00:00" \
           "alice" "alice@home.local"

# 5: Commit by Bob with different old email (personal)
echo "Bobby's personal contribution" >> file2.txt
git add file2.txt
git_commit "Bobby's weekend work" \
           "2023-01-01T16:00:00+00:00" \
           "Bobby" "bobby@personal.net"

# 6: Commit by Carol with old company email
echo "C. Coder's legacy code" > file3.txt
git add file3.txt
git_commit "C. Coder imports old code" \
           "2023-01-01T17:00:00+00:00" \
           "C. Coder" "ccoder@oldcompany.org"

# 7: Commit by Dave with old email
echo "Dave's infrastructure" > file4.txt
git add file4.txt
git_commit "Dave sets up infrastructure" \
           "2023-01-01T18:00:00+00:00" \
           "Dave" "dave@example.com"

###############################################################################
# Commits with new/canonical email addresses (should match after mailmap)
###############################################################################

# 8: Commit by Alice with canonical email
echo "Alice canonical" >> file1.txt
git add file1.txt
git_commit "Alice with canonical email" \
           "2023-01-01T19:00:00+00:00" \
           "Alice Developer" "alice@company.com"

# 9: Commit by Bob with canonical email
echo "Bob canonical" >> file2.txt
git add file2.txt
git_commit "Bob with canonical email" \
           "2023-01-01T20:00:00+00:00" \
           "Bob Builder" "bob@company.com"

# 10: Commit by Carol with canonical email
echo "Carol canonical" >> file3.txt
git add file3.txt
git_commit "Carol with canonical email" \
           "2023-01-01T21:00:00+00:00" \
           "Carol Coder" "carol@company.com"

# 11: Final commit by Dave with canonical email
echo "Dave canonical" >> file4.txt
git add file4.txt
git_commit "Dave with canonical email" \
           "2023-01-01T22:00:00+00:00" \
           "Dave DevOps" "dave@company.com"

exit 0