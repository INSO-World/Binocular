@echo off
setlocal enabledelayedexpansion

REM Usage check
if "%~1"=="" (
  echo Usage: %~nx0 ^<directory^>
  exit /b 1
)

set REPO_DIR=%1
if exist "%REPO_DIR%" rmdir /s /q "%REPO_DIR%"
mkdir "%REPO_DIR%"
pushd "%REPO_DIR%"
git init -q -b master

REM Helper to commit with fixed author/committer data

REM 1: Initial commit by Alice
echo Hello, world! > file1.txt
git add file1.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Initial commit" --date="2023-01-01T12:01:00+00:00" -q

REM 2: Append to file1.txt by Bob
echo Additional content>>file1.txt
git add file1.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Append to file1.txt" --date="2023-01-01T13:00:00+00:00" -q

REM 3: Add file2.txt by Carol
echo This is file2 > file2.txt
git add file2.txt
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Add file2.txt" --date="2023-01-01T14:00:00+00:00" -q

REM 4: Modify file2.txt by Alice
echo More content for file2>>file2.txt
git add file2.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Modify file2.txt" --date="2023-01-01T15:00:00+00:00" -q

REM 5: Rename file1.txt to file1-renamed.txt by Bob
ren file1.txt file1-renamed.txt
git add file1-renamed.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Rename file1.txt to file1-renamed.txt" --date="2023-01-01T16:00:00+00:00" -q

REM 6: Delete file2.txt by Carol
del file2.txt
git rm file2.txt
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Delete file2.txt" --date="2023-01-01T17:00:00+00:00" -q

REM 7: Create file3.txt by Alice
echo Content of file3 > file3.txt
git add file3.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Create file3.txt" --date="2023-01-01T18:05:00+00:00" -q

REM 8: Update file3.txt by Bob
echo Appending more to file3>>file3.txt
git add file3.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Update file3.txt with more content" --date="2023-01-01T19:00:00+00:00" -q

REM 9: Create dir1 and add file4.txt by Carol
mkdir dir1
echo Inside dir1 > dir1\file4.txt
git add dir1\file4.txt
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Create dir1 and add file4.txt" --date="2023-01-01T20:00:00+00:00" -q

REM 10: Rename file4.txt inside dir1 by Alice
ren dir1\file4.txt file4-renamed.txt
git add dir1\file4-renamed.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Rename file4.txt to file4-renamed.txt in dir1" --date="2023-01-01T21:00:00+00:00" -q

REM 11: Add a deterministic binary blob by Bob
powershell -Command "[byte[]]$b=0..99; Set-Content -Path file5.bin -Value $b -Encoding Byte"
git add file5.bin
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Add binary file file5.bin" --date="2023-01-01T22:00:00+00:00" -q

REM 12: Delete file3.txt by Carol
del file3.txt
git rm file3.txt
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Delete file3.txt" --date="2023-01-01T23:00:00+00:00" -q

REM 13: Insert a line in file1-renamed.txt by Alice
powershell -Command "(Get-Content file1-renamed.txt) | ForEach-Object {if ($_.ReadCount -eq 1) {$_; 'Inserted line'} else {$_}} | Set-Content file1-renamed.txt"
git add file1-renamed.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Modify file1-renamed.txt by inserting a line" --date="2023-01-02T00:00:00+00:00" -q

REM 14: Re-add file2.txt with new content by Bob
echo Recreated file2 > file2.txt
git add file2.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Re-add file2.txt with new content" --date="2023-01-02T01:00:00+00:00" -q

REM 15: Final multi-file update by Carol
echo Final update to file1-renamed.txt>>file1-renamed.txt
echo Final update to file2.txt>>file2.txt
git add file1-renamed.txt file2.txt
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Final update: modify multiple files" --date="2023-01-02T02:00:00+00:00" -q

REM Orphan commit from another remote + merge
git checkout --orphan imported -q
del /q *.*
echo Imported commit content > imported.txt
git add imported.txt
git -c "user.name=Dave" -c "user.email=dave@example.com" commit -m "Imported commit: independent history from another remote" --date="2023-01-03T00:00:00+00:00" -q

git checkout master -q
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Merge imported history from remote" --date="2023-01-03T01:00:00+00:00" -q
git merge --allow-unrelated-histories imported -m "Imported commit: independent history from another remote" -q

REM Classical branch merges
git checkout -b feature -q
echo Feature update: appended line>>file1-renamed.txt
git add file1-renamed.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Feature: update file1-renamed.txt" --date="2023-01-02T03:00:00+00:00" -q

echo Content for file6 from feature branch > file6.txt
git add file6.txt
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Feature: add file6.txt" --date="2023-01-02T03:30:00+00:00" -q

git checkout master -q
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Merge branch 'feature'" --date="2023-01-02T04:00:00+00:00" -q
git merge --no-ff feature -m "Feature: add file6.txt" -q

git checkout -b bugfix -q
echo Bugfix: corrected a typo in file2.txt>>file2.txt
git add file2.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Bugfix: update file2.txt with correction" --date="2023-01-02T04:30:00+00:00" -q

echo Bugfix: final adjustment to file2.txt>>file2.txt
git add file2.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Bugfix: further update to file2.txt" --date="2023-01-02T05:00:00+00:00" -q

git checkout master -q
git -c "user.name=Carol" -c "user.email=carol@example.com" commit -m "Merge branch 'bugfix'" --date="2023-01-02T05:30:00+00:00" -q
git merge --no-ff bugfix -m "Bugfix: further update to file2.txt" -q

REM Octopus merge of three branches
for %%b in (octo1 octo2 octo3) do (
  git checkout -b %%b master -q
  echo Change from %%b > %%b.txt
  git add %%b.txt
  if "%%b"=="octo1" (
    set name=Alice
    set email=alice@example.com
    set date=2023-01-02T06:00:00+00:00
  ) else if "%%b"=="octo2" (
    set name=Bob
    set email=bob@example.com
    set date=2023-01-02T06:30:00+00:00
  ) else if "%%b"=="octo3" (
    set name=Carol
    set email=carol@example.com
    set date=2023-01-02T07:00:00+00:00
  )
  git -c "user.name=!name!" -c "user.email=!email!" commit -m "Octo !b!: Add !b!.txt" --date="!date!" -q
)
git checkout master -q
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Octopus merge of octo1, octo2, and octo3" --date="2023-01-02T07:30:00+00:00" -q
git merge --no-ff octo1 octo2 octo3 -m "Octopus merge of octo1, octo2, and octo3" -q

popd
exit /b 0 