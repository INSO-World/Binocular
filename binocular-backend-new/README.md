# Binocular Backend

## Requirements
- Install `ktlint`: https://pinterest.github.io/ktlint/latest/quick-start/. 
  - Maven plugin reference: https://gantsign.com/ktlint-maven-plugin/plugin-info.html 
  - Use `mvn ktlint:check` to check for errors
  - Use `mvn ktlint:format` to automatically fixes violations of the code style (when possible)
- Docker running in the background, otherwise tests will fail (tests in `infrastructure-sql/arangodb/tests` use TestContainers internally)

© INSO/BUSY 09/2025
