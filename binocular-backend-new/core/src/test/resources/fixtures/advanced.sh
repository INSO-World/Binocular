#!/usr/bin/env bash
set -euo pipefail

export TZ=UTC

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <directory>"
  exit 1
fi

REPO_DIR="$1"
mkdir -p "$REPO_DIR"
cd "$REPO_DIR"

git init -q -b master

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

echo "Hello, world!" > file1.txt
git add file1.txt
git_commit "Initial commit" "2023-01-01T12:01:00+00:00" "Alice" "alice@example.com"

echo "Additional content" >> file1.txt
git add file1.txt
git_commit "Append to file1.txt" "2023-01-01T13:00:00+00:00" "Bob" "bob@example.com"

echo "This is file2" > file2.txt
git add file2.txt
git_commit "Add file2.txt" "2023-01-01T14:00:00+00:00" "Carol" "carol@example.com"

echo "More content for file2" >> file2.txt
git add file2.txt
git_commit "Modify file2.txt" "2023-01-01T15:00:00+00:00" "Alice" "alice@example.com"

git mv file1.txt file1-renamed.txt
git_commit "Rename file1.txt to file1-renamed.txt" "2023-01-01T16:00:00+00:00" "Bob" "bob@example.com"

git rm file2.txt
git_commit "Delete file2.txt" "2023-01-01T17:00:00+00:00" "Carol" "carol@example.com"

echo "Content of file3" > file3.txt
git add file3.txt
GIT_AUTHOR_DATE="2023-01-01T18:00:00+00:00" git_commit "Create file3.txt" "2023-01-01T18:05:00+00:00" "Alice" "alice@example.com"

echo "Appending more to file3" >> file3.txt
git add file3.txt
git_commit "Update file3.txt with more content" "2023-01-01T19:00:00+00:00" "Bob" "bob@example.com"

mkdir -p dir1
echo "Inside dir1" > dir1/file4.txt
git add dir1/file4.txt
git_commit "Create dir1 and add file4.txt" "2023-01-01T20:00:00+00:00" "Carol" "carol@example.com"

git mv dir1/file4.txt dir1/file4-renamed.txt
git_commit "Rename file4.txt to file4-renamed.txt in dir1" "2023-01-01T21:00:00+00:00" "Alice" "alice@example.com"

dd if=/dev/zero bs=100 count=1 of=file5.bin status=none
git add file5.bin
git_commit "Add binary file file5.bin" "2023-01-01T22:00:00+00:00" "Bob" "bob@example.com"

git rm file3.txt
git_commit "Delete file3.txt" "2023-01-01T23:00:00+00:00" "Carol" "carol@example.com"

awk 'NR==1{print; print "Inserted line"; next}1' file1-renamed.txt > tmp && mv tmp file1-renamed.txt
git add file1-renamed.txt
git_commit "Modify file1-renamed.txt by inserting a line" "2023-01-02T00:00:00+00:00" "Alice" "alice@example.com"

echo "Recreated file2" > file2.txt
git add file2.txt
GIT_AUTHOR_DATE="2023-01-02T00:30:00+00:00" git_commit "Re-add file2.txt with new content" "2023-01-02T01:00:00+00:00" "Bob" "bob@example.com"

echo "Final update to file1-renamed.txt" >> file1-renamed.txt
echo "Final update to file2.txt" >> file2.txt
git add file1-renamed.txt file2.txt
git_commit "Final update: modify multiple files" "2023-01-02T02:00:00+00:00" "Carol" "carol@example.com"

git checkout --orphan imported -q
git rm -rf . > /dev/null 2>&1 || true
echo "Imported commit content" > imported.txt
git add imported.txt
git_commit "Imported commit: independent history from another remote" "2023-01-03T00:00:00+00:00" "Dave" "dave@example.com"

git checkout master -q
git_commit "Merge imported history from remote" "2023-01-03T01:00:00+00:00" "Alice" "alice@example.com" && \
  git merge --allow-unrelated-histories imported -m "$(git log -1 --pretty=%B)" -q

git checkout -b feature -q
echo "Feature update: appended line" >> file1-renamed.txt
git add file1-renamed.txt
git_commit "Feature: update file1-renamed.txt" "2023-01-02T03:00:00+00:00" "Bob" "bob@example.com"

echo "Content for file6 from feature branch" > file6.txt
git add file6.txt
git_commit "Feature: add file6.txt" "2023-01-02T03:30:00+00:00" "Carol" "carol@example.com"

git checkout master -q
git_commit "Merge branch 'feature'" "2023-01-02T04:00:00+00:00" "Alice" "alice@example.com" && \
  git merge --no-ff feature -m "$(git log -1 --pretty=%B)" -q

git checkout -b bugfix -q
echo "Bugfix: corrected a typo in file2.txt" >> file2.txt
git add file2.txt
git_commit "Bugfix: update file2.txt with correction" "2023-01-02T04:30:00+00:00" "Alice" "alice@example.com"

echo "Bugfix: final adjustment to file2.txt" >> file2.txt
git add file2.txt
git_commit "Bugfix: further update to file2.txt" "2023-01-02T05:00:00+00:00" "Bob" "bob@example.com"

git checkout master -q
git_commit "Merge branch 'bugfix'" "2023-01-02T05:30:00+00:00" "Carol" "carol@example.com" && \
  git merge --no-ff bugfix -m "$(git log -1 --pretty=%B)" -q

for b in octo1 octo2 octo3; do
  git checkout -b "$b" master -q
  echo "Change from $b" > "$b".txt
  git add "$b".txt
  case "$b" in
    octo1) name="Alice"; email="alice@example.com"; date="2023-01-02T06:00:00+00:00";;
    octo2) name="Bob";   email="bob@example.com";   date="2023-01-02T06:30:00+00:00";;
    octo3) name="Carol"; email="carol@example.com"; date="2023-01-02T07:00:00+00:00";;
  esac
  git_commit "Octo ${b: -1}: Add ${b}.txt" "$date" "$name" "$email"
done

git checkout master -q
GIT_AUTHOR_DATE="2023-01-02T07:30:00+00:00" GIT_COMMITTER_DATE="2023-01-02T07:30:00+00:00" \
GIT_AUTHOR_NAME="Alice" GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_COMMITTER_NAME="Alice" GIT_COMMITTER_EMAIL="alice@example.com" \
  git merge --no-ff octo1 octo2 octo3 -m "Octopus merge of octo1, octo2, and octo3" -q

# extended commits
echo "Remove the inserted line" > tmp && sed '/Inserted line/d' file1-renamed.txt > tmp && mv tmp file1-renamed.txt
git add file1-renamed.txt
git_commit "Remove inserted line from file1-renamed.txt" "2023-01-02T08:00:00+00:00" "Bob" "bob@example.com"

echo "Post-merge note" >> file6.txt
git add file6.txt
git_commit "Append post-merge note to file6.txt" "2023-01-02T08:30:00+00:00" "Dave" "dave@example.com"


# 3 additional commits on 'imported' branch
git checkout imported -q

echo "Imported update 1" >> imported.txt
git add imported.txt
git_commit "Imported: update 1 to imported.txt" "2023-01-03T02:00:00+00:00" "Dave" "dave@example.com"

echo "Imported update 2" >> imported.txt
git add imported.txt
git_commit "Imported: update 2 to imported.txt" "2023-01-03T02:30:00+00:00" "Carol" "carol@example.com"

echo "Imported update 3" >> imported.txt
git add imported.txt
git_commit "Imported: update 3 to imported.txt" "2023-01-03T03:00:00+00:00" "Bob" "bob@example.com"

# Merge 'imported' back into master again
git checkout master -q
GIT_AUTHOR_DATE="2023-01-03T03:30:00+00:00" GIT_COMMITTER_DATE="2023-01-03T03:30:00+00:00" \
GIT_AUTHOR_NAME="Alice" GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_COMMITTER_NAME="Alice" GIT_COMMITTER_EMAIL="alice@example.com" \
  git merge --no-ff imported -m "Second merge of imported history" -q --allow-unrelated-histories

# 3 commits on master post-import
echo "Master post-import 1" >> file1-renamed.txt
git add file1-renamed.txt
git_commit "Master: post-import update 1" "2023-01-03T04:00:00+00:00" "Alice" "alice@example.com"

echo "Master post-import 2" >> file2.txt
git add file2.txt
git_commit "Master: post-import update 2" "2023-01-03T04:30:00+00:00" "Bob" "bob@example.com"

echo "Master post-import 3" >> file6.txt
git add file6.txt
git_commit "Master: post-import update 3" "2023-01-03T05:00:00+00:00" "Carol" "carol@example.com"

# 5 commits on 'extra' branch and merge into master

git checkout -b extra -q
# 1/5
echo "Extra change 1" >> file1-renamed.txt
git add file1-renamed.txt
git_commit "Extra: change 1" "2023-01-03T06:00:00+00:00" "Alice" "alice@example.com"
# 2/5
echo "Extra change 2" >> file2.txt
git add file2.txt
git_commit "Extra: change 2" "2023-01-03T06:30:00+00:00" "Bob" "bob@example.com"
# 3/5
echo "Extra change 3" > file7.txt
git add file7.txt
git_commit "Extra: add file7.txt" "2023-01-03T07:00:00+00:00" "Carol" "carol@example.com"
# 4/5
echo "Extra change 4" >> file7.txt
git add file7.txt
git_commit "Extra: update file7.txt" "2023-01-03T07:30:00+00:00" "Alice" "alice@example.com"
# 5/5
git rm file5.bin
git_commit "Extra: remove file5.bin" "2023-01-03T08:00:00+00:00" "Carol" "carol@example.com"

git checkout master -q
GIT_AUTHOR_DATE="2023-01-03T08:30:00+00:00" GIT_COMMITTER_DATE="2023-01-03T08:30:00+00:00" \
GIT_AUTHOR_NAME="Alice" GIT_AUTHOR_EMAIL="alice@example.com" \
GIT_COMMITTER_NAME="Alice" GIT_COMMITTER_EMAIL="alice@example.com" \
  git merge --no-ff extra -m "Merge branch 'extra' with five extra changes" -q

exit 0
