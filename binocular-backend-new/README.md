# Binocular Backend

## Requirements
- Install `ktlint`: https://pinterest.github.io/ktlint/latest/quick-start/. 
  - Maven plugin reference: https://gantsign.com/ktlint-maven-plugin/plugin-info.html 
  - Use `mvn ktlint:check` to check for errors
  - Use `mvn ktlint:format` to automatically fixes violations of the code style (when possible)
- Docker running in the background, otherwise tests will fail (tests in `infrastructure-sql/arangodb/tests` use TestContainers internally)

## Hints for git-lfs

1. Install git-lfs
2. call `git lfs pull` for first clone or weird errors like "ELF" when running tests. Check file size of libraries in `ffi/src/main/resources/.../<.ddl/.so/.dylib>`. Errors might result due to not pulling lfs content
3. **Run LFS migration on entire history**<br/>
When editing `.gitattributes` rewrite history to fix lfs for previously committed files which are now lfs, e.g.
```bash
git lfs migrate import \ 
  --include='binocular-backend-new/web/src/test/resources/realdata/db_dump/**' \
  --include='binocular-backend-new/ffi/src/main/resources/**' \
  --everything
```
`--everything` rewrites all branches, not just HEAD. This converts all past commits where these files existed into LFS pointers.
4. **Push the rewritten history**<br/>
`git push origin feature/fixing-be-new --force-with-lease`
`--force-with-lease` is safer than plain --force — it checks no remote work was lost.
Force is disabled for `develop` and `main` for good reason!

© INSO/BUSY 09/2025
