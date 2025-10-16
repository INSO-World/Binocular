#!/bin/bash
# This script creates a dummy Git repository with multiple commits,
# including file operations, different authors/timestamps, classical branch merges,
# an octopus merge (merging three branches in a single merge commit),
# and an imported orphan commit that simulates a commit merged from another remote.
#
# Usage: ./make-dummy-repo.sh /path/to/repo-directory
#
# The script runs quietly (unless an error occurs).

set -euo pipefail

# Force Git to use UTC for all timestamps
export TZ=UTC

# Verify that exactly one argument (the target directory) is provided.
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <directory>"
  exit 1
fi

REPO_DIR="$1"
mkdir -p "$REPO_DIR"
cd "$REPO_DIR"

# Initialize an empty Git repository quietly.
git init -q

# Determine the default branch name (could be 'main' or 'master')
MAIN_BRANCH="master"

###############################################################################
# Commits 1-15: Initial history with various operations and authors
###############################################################################

# Commit 1: Initial commit by Alice – create file1.txt
echo "Hello, world!" > file1.txt
git add file1.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-01T12:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-01T12:00:00" \
git commit -m "Initial commit" -q

# Commit 2: Append to file1.txt by Bob
echo "Additional content" >> file1.txt
git add file1.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-01T13:00:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-01T13:00:00" \
git commit -m "Append to file1.txt" -q

# Commit 3: Add file2.txt by Carol
echo "This is file2" > file2.txt
git add file2.txt
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-01T14:00:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-01T14:00:00" \
git commit -m "Add file2.txt" -q

# Commit 4: Modify file2.txt by Alice
echo "More content for file2" >> file2.txt
git add file2.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-01T15:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-01T15:00:00" \
git commit -m "Modify file2.txt" -q

# Commit 5: Rename file1.txt to file1-renamed.txt by Bob
git mv file1.txt file1-renamed.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-01T16:00:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-01T16:00:00" \
git commit -m "Rename file1.txt to file1-renamed.txt" -q

# Commit 6: Delete file2.txt by Carol
git rm file2.txt -q
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-01T17:00:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-01T17:00:00" \
git commit -m "Delete file2.txt" -q

# Commit 7: Create file3.txt by Alice (with differing author/committer times)
echo "Content of file3" > file3.txt
git add file3.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-01T18:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-01T18:05:00" \
git commit -m "Create file3.txt" -q

# Commit 8: Update file3.txt with more content by Bob
echo "Appending more to file3" >> file3.txt
git add file3.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-01T19:00:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-01T19:00:00" \
git commit -m "Update file3.txt with more content" -q

# Commit 9: Create directory 'dir1' and add file4.txt inside by Carol
mkdir dir1
echo "Inside dir1" > dir1/file4.txt
git add dir1/file4.txt
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-01T20:00:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-01T20:00:00" \
git commit -m "Create dir1 and add file4.txt" -q

# Commit 10: Rename file in dir1: file4.txt to file4-renamed.txt by Alice
git mv dir1/file4.txt dir1/file4-renamed.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-01T21:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-01T21:00:00" \
git commit -m "Rename file4.txt to file4-renamed.txt in dir1" -q

# Commit 11: Create binary file file5.bin by Bob
head -c 100 /dev/urandom > file5.bin
git add file5.bin
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-01T22:00:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-01T22:00:00" \
git commit -m "Add binary file file5.bin" -q

# Commit 12: Delete file3.txt by Carol
git rm file3.txt -q
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-01T23:00:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-01T23:00:00" \
git commit -m "Delete file3.txt" -q

# Commit 13: Modify file1-renamed.txt by inserting a line in the middle by Alice
awk 'NR==1{print; print "Inserted line"; next} {print}' file1-renamed.txt > tmp && mv tmp file1-renamed.txt
git add file1-renamed.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-02T00:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-02T00:00:00" \
git commit -m "Modify file1-renamed.txt by inserting a line" -q

# Commit 14: Re-add file2.txt with new content by Bob (with mismatched author/committer times)
echo "Recreated file2" > file2.txt
git add file2.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-02T00:30:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-02T01:00:00" \
git commit -m "Re-add file2.txt with new content" -q

# Commit 15: Final update modifying multiple files by Carol
echo "Final update to file1-renamed.txt" >> file1-renamed.txt
echo "Final update to file2.txt" >> file2.txt
git add file1-renamed.txt file2.txt
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-02T02:00:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-02T02:00:00" \
git commit -m "Final update: modify multiple files" -q

###############################################################################
# Orphan Commit from another remote repository (independent history)
###############################################################################
# Create an orphan branch "imported" with no ancestors.
git checkout --orphan imported -q
# Remove any tracked files from the index (ignore errors if none exist)
git rm -rf . > /dev/null 2>&1 || true
# Add a new file unique to the imported history.
echo "Imported commit content" > imported.txt
git add imported.txt
GIT_AUTHOR_NAME="Dave" \
GIT_AUTHOR_EMAIL="dave@example.com" \
GIT_AUTHOR_DATE="2023-01-03T00:00:00" \
GIT_COMMITTER_NAME="Dave" \
GIT_COMMITTER_EMAIL="dave@example.com" \
GIT_COMMITTER_DATE="2023-01-03T00:00:00" \
git commit -m "Imported commit: independent history from another remote" -q

# Merge the imported orphan branch into the main branch, allowing unrelated histories.
git checkout -q "$MAIN_BRANCH"
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-03T01:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-03T01:00:00" \
git merge --allow-unrelated-histories imported -m "Merge imported history from remote" -q

###############################################################################
# Classical Merge Commits
###############################################################################

# ----- Branch "feature" -----
# Create branch "feature" from the current HEAD.
git checkout -b feature -q

# Commit 16: On branch 'feature', append a line to file1-renamed.txt.
echo "Feature update: appended line" >> file1-renamed.txt
git add file1-renamed.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-02T03:00:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-02T03:00:00" \
git commit -m "Feature: update file1-renamed.txt" -q

# Commit 17: On branch 'feature', create a new file file6.txt.
echo "Content for file6 from feature branch" > file6.txt
git add file6.txt
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-02T03:30:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-02T03:30:00" \
git commit -m "Feature: add file6.txt" -q

# Switch back to the main branch.
git checkout -q "$MAIN_BRANCH"

# Merge branch 'feature' into main with a merge commit.
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-02T04:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-02T04:00:00" \
git merge --no-ff feature -m "Merge branch 'feature'" -q

# ----- Branch "bugfix" -----
# Create branch "bugfix" from the current HEAD.
git checkout -b bugfix -q

# Commit 18: On branch 'bugfix', modify file2.txt by appending a bugfix line.
echo "Bugfix: corrected a typo in file2.txt" >> file2.txt
git add file2.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-02T04:30:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-02T04:30:00" \
git commit -m "Bugfix: update file2.txt with correction" -q

# Commit 19: Further modify file2.txt on the 'bugfix' branch.
echo "Bugfix: final adjustment to file2.txt" >> file2.txt
git add file2.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-02T05:00:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-02T05:00:00" \
git commit -m "Bugfix: further update to file2.txt" -q

# Switch back to the main branch.
git checkout -q "$MAIN_BRANCH"

# Merge branch 'bugfix' into main with a merge commit.
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-02T05:30:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-02T05:30:00" \
git merge --no-ff bugfix -m "Merge branch 'bugfix'" -q

###############################################################################
# Octopus Merge: Merge three branches in one commit
###############################################################################

# Create branch "octo1" from main and add a new file.
git checkout -b octo1 -q
echo "Change from octo1" > octo1.txt
git add octo1.txt
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-02T06:00:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-02T06:00:00" \
git commit -m "Octo1: Add octo1.txt" -q

# Return to main branch.
git checkout -q "$MAIN_BRANCH"

# Create branch "octo2" from main and add a new file.
git checkout -b octo2 -q
echo "Change from octo2" > octo2.txt
git add octo2.txt
GIT_AUTHOR_NAME="Bob" \
GIT_AUTHOR_EMAIL="bob@example.com" \
GIT_AUTHOR_DATE="2023-01-02T06:30:00" \
GIT_COMMITTER_NAME="Bob" \
GIT_COMMITTER_EMAIL="bob@example.com" \
GIT_COMMITTER_DATE="2023-01-02T06:30:00" \
git commit -m "Octo2: Add octo2.txt" -q

# Return to main branch.
git checkout -q "$MAIN_BRANCH"

# Create branch "octo3" from main and add a new file.
git checkout -b octo3 -q
echo "Change from octo3" > octo3.txt
git add octo3.txt
GIT_AUTHOR_NAME="Carol" \
GIT_AUTHOR_EMAIL="carol@example.com" \
GIT_AUTHOR_DATE="2023-01-02T07:00:00" \
GIT_COMMITTER_NAME="Carol" \
GIT_COMMITTER_EMAIL="carol@example.com" \
GIT_COMMITTER_DATE="2023-01-02T07:00:00" \
git commit -m "Octo3: Add octo3.txt" -q

# Return to main branch before merging.
git checkout -q "$MAIN_BRANCH"

# Perform the octopus merge (merging octo1, octo2, and octo3 in one commit).
GIT_AUTHOR_NAME="Alice" \
GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_AUTHOR_DATE="2023-01-02T07:30:00" \
GIT_COMMITTER_NAME="Alice" \
GIT_COMMITTER_EMAIL="alice@example.com" \
GIT_COMMITTER_DATE="2023-01-02T07:30:00" \
git merge --no-ff octo1 octo2 octo3 -m "Octopus merge of octo1, octo2, and octo3" -q

# End of script.
exit 0
