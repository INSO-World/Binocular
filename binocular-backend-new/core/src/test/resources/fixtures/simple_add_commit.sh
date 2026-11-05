#!/usr/bin/env bash
set -euo pipefail

# Force Git to use UTC for all timestamps
export TZ=UTC

# Usage check
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <directory>"
  exit 1
fi

REPO_DIR="$(pwd)/$1"
REMOTE_DIR="${REPO_DIR}_remote.git"

cd "$REPO_DIR"

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

# 14: Re-add file2.txt with new content by Bob
echo "Added file xyz" > xyz.txt
git add xyz.txt
GIT_AUTHOR_DATE="2024-01-02T00:30:00+00:00" \
git_commit "Added file.xyz" \
           "2024-01-02T01:00:00+00:00" \
           "Dave" "dave@example.com"

exit 0
