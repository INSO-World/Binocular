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

# Create bare repository to act as dummy remote
mkdir -p "$REMOTE_DIR"
git init --bare "$REMOTE_DIR" >/dev/null 2>&1

# Create and populate local repository
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

# Add dummy remote reference
git remote add origin "$REMOTE_DIR"

###############################################################################
# Commits 1–15: Initial history
###############################################################################

# 1: Initial commit by Alice
echo "Hello, world!" > file1.txt
git add file1.txt
git_commit "Initial commit" \
           "2023-01-01T12:00:00+00:00" \
           "Alice" "alice@example.com"

git push -u origin master -q

# 2: Append to file1.txt by Bob
echo "Additional content" >> file1.txt
git add file1.txt
git_commit "Append to file1.txt" \
           "2023-01-01T13:00:00+00:00" \
           "Bob" "bob@example.com"

# 3: Add file2.txt by Carol
echo "This is file2" > file2.txt
git add file2.txt
git_commit "Add file2.txt" \
           "2023-01-01T14:00:00+00:00" \
           "Carol" "carol@example.com"

# 4: Modify file2.txt by Alice
echo "More content for file2" >> file2.txt
git add file2.txt
git_commit "Modify file2.txt" \
           "2023-01-01T15:00:00+00:00" \
           "Alice" "alice@example.com"

# 5: Rename file1.txt to file1-renamed.txt by Bob
git mv file1.txt file1-renamed.txt
git_commit "Rename file1.txt to file1-renamed.txt" \
           "2023-01-01T16:00:00+00:00" \
           "Bob" "bob@example.com"

# 6: Delete file2.txt by Carol
git rm file2.txt
git_commit "Delete file2.txt" \
           "2023-01-01T17:00:00+00:00" \
           "Carol" "carol@example.com"

# 7: Create file3.txt by Alice (with differing author/committer times)
echo "Content of file3" > file3.txt
git add file3.txt
GIT_AUTHOR_DATE="2023-01-01T18:00:00+00:00" \
git_commit "Create file3.txt" \
           "2023-01-01T18:05:00+00:00" \
           "Alice" "alice@example.com"

# 8: Update file3.txt by Bob
echo "Appending more to file3" >> file3.txt
git add file3.txt
git_commit "Update file3.txt with more content" \
           "2023-01-01T19:00:00+00:00" \
           "Bob" "bob@example.com"

# 9: Create dir1 and add file4.txt by Carol
mkdir -p dir1
echo "Inside dir1" > dir1/file4.txt
git add dir1/file4.txt
git_commit "Create dir1 and add file4.txt" \
           "2023-01-01T20:00:00+00:00" \
           "Carol" "carol@example.com"

# 10: Rename file4.txt inside dir1 by Alice
git mv dir1/file4.txt dir1/file4-renamed.txt
git_commit "Rename file4.txt to file4-renamed.txt in dir1" \
           "2023-01-01T21:00:00+00:00" \
           "Alice" "alice@example.com"

# 11: Add a deterministic binary blob by Bob
dd if=/dev/zero bs=100 count=1 of=file5.bin status=none
git add file5.bin
git_commit "Add binary file file5.bin" \
           "2023-01-01T22:00:00+00:00" \
           "Bob" "bob@example.com"

# 12: Delete file3.txt by Carol
git rm file3.txt
git_commit "Delete file3.txt" \
           "2023-01-01T23:00:00+00:00" \
           "Carol" "carol@example.com"

# 13: Insert a line in file1-renamed.txt by Alice
awk 'NR==1{print; print "Inserted line"; next}1' file1-renamed.txt > tmp && mv tmp file1-renamed.txt
git add file1-renamed.txt
git_commit "Modify file1-renamed.txt by inserting a line" \
           "2023-01-02T00:00:00+00:00" \
           "Alice" "alice@example.com"

# Push current branches to remote
git push origin master -q

# 14: Re-add file2.txt with new content by Bob
echo "Recreated file2" > file2.txt
git add file2.txt
GIT_AUTHOR_DATE="2023-01-02T00:30:00+00:00" \
git_commit "Re-add file2.txt with new content" \
           "2023-01-02T01:00:00+00:00" \
           "Bob" "bob@example.com"

exit 0
