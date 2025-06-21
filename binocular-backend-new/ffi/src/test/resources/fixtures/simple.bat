@echo off
setlocal enabledelayedexpansion

REM Usage check
if "%~1"=="" (
  echo Usage: %~nx0 ^<directory^>
  exit /b 1
)

set REPO_DIR=%cd%\%1
set REMOTE_DIR=%REPO_DIR%_remote.git

REM Remove old directories
if exist "%REPO_DIR%" rmdir /s /q "%REPO_DIR%"
if exist "%REMOTE_DIR%" rmdir /s /q "%REMOTE_DIR%"

REM Create bare repository to act as dummy remote
mkdir "%REMOTE_DIR%"
pushd "%REMOTE_DIR%"
git init --bare >nul 2>&1
popd

REM Create and populate local repository
mkdir "%REPO_DIR%"
pushd "%REPO_DIR%"
git init -q -b master

REM Helper to commit with fixed author/committer data
setlocal
set GIT_AUTHOR_NAME=Alice
set GIT_AUTHOR_EMAIL=alice@example.com
set GIT_COMMITTER_NAME=Alice
set GIT_COMMITTER_EMAIL=alice@example.com

REM 1: Initial commit by Alice
echo Hello, world! > file1.txt
git add file1.txt
git -c "user.name=Alice" -c "user.email=alice@example.com" commit -m "Initial commit" --date="2023-01-01T12:00:00+00:00" -q

git remote add origin "%REMOTE_DIR%"
git push -u origin master -q

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

git push origin master -q

REM 14: Re-add file2.txt with new content by Bob
echo Recreated file2 > file2.txt
git add file2.txt
git -c "user.name=Bob" -c "user.email=bob@example.com" commit -m "Re-add file2.txt with new content" --date="2023-01-02T01:00:00+00:00" -q

popd
exit /b 0 