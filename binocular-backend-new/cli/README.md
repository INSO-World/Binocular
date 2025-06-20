# Binocular CLI package

## Starting Spring-Boot Application
_In the root folder (binocular-backend-new):_
```bash
./mvnw clean spring-boot:run
```
## Building the Application
```bash
./mvnw clean package -DskipTests -pl cli -am
```
### Starting the Fat-Jar
```bash
java -jar -Dspring.profiles.active=postgres -Djava.library.path=. ./cli/target/cli-0.0.1-SNAPSHOT.jar index commits -b develop
```
