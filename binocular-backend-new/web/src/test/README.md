# Running tests

Test can only be run if 
- a maven profile is active (`-Parangodb` or `-Ppostgres`)
- and spring profiles are set correctly, e.g. `-Dspring.profiles.active=test,arangodb` or `-Dspring.profiles.active=test,postgres`
  - Spring Profiles must also be set in IntelliJ -> Run Config -> VM Options
