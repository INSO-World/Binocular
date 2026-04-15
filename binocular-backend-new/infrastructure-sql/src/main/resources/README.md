# Liquibase maven commands



## Configure database access
- Edit `liqubase.properties`

## Drop all tables
- `mvn liquibase:dropAll -Ppostgres`

## Run everything
- "everything" is run as specified in `db.changelog/db.changelog-master.yaml`
- `mvn liquibase:update -Ppostgres`

## Rollback
- `rollback` should be specified for changes to enable reverting changes!
- See https://www.baeldung.com/liquibase-rollback
- To rollback one step after another execute 
`mvn liquibase:rollback -Ppostgres -Dliquibase.rollbackCount=1`
