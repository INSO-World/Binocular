# Binocular CLI Tests

This directory contains all tests for the Binocular CLI application. The tests are organized into unit tests and integration tests, with unit tests tagged with `@Tag("unit")`.

## Running Tests with Maven

### Unit Tests
Unit tests are tagged with `@Tag("unit")` and can be run using:

```bash
# Run all unit tests
mvn test -Dgroups=unit

# Run specific unit test class
mvn test -Dgroups=unit -Dtest=YourUnitTest

# Run specific unit test method
mvn test -Dgroups=unit -Dtest=YourUnitTest#testMethod
```

### Integration Tests
Integration tests are tagged with `@Tag("integration")` and can be run using:

```bash
# Run all integration tests
mvn verify -Dgroups=integration

# Run specific integration test class
mvn verify -Dgroups=integration -Dtest=YourIntegrationTest

# Run specific integration test method
mvn verify -Dgroups=integration -Dtest=YourIntegrationTest#testMethod
```

### Test Coverage
```bash
# Generate test coverage report for unit tests
mvn verify jacoco:report -Dgroups=unit

# Generate test coverage report for integration tests
mvn verify jacoco:report -Dgroups=integration

# View coverage report
open target/site/jacoco/index.html
```

### Common Maven Test Options
```bash
# Skip tests
mvn clean install -DskipTests

# Skip unit tests but run integration tests
mvn verify -Dgroups=integration

# Run tests in parallel
mvn test -Dparallel=true -Dgroups=unit

# Set test timeout
mvn test -Dtest.timeout=30 -Dgroups=unit
```

### Test Profiles
```bash
# Run unit tests with specific profile
mvn test -P unit-tests -Dgroups=unit

# Run integration tests with specific profile
mvn verify -P integration-tests -Dgroups=integration

# Run tests with multiple profiles
mvn verify -P unit-tests,integration-tests -Dgroups=unit,integration
```

## Test Structure
- `kotlin/`: Contains all test classes
- `resources/`: Contains test resources and fixtures

For more detailed information about the test structure and organization, see the README in the `kotlin` directory.

## Adding New Tests

When adding new tests, make sure to:
1. Tag unit tests with `@Tag("unit")`
2. Tag integration tests with `@Tag("integration")`
3. Place unit tests in the appropriate package under `kotlin/`
4. Place integration tests in the appropriate package under `kotlin/`
5. Add any required test resources to the `resources/` directory

Example of a unit test:
```kotlin
@Tag("unit")
class YourUnitTest {
    @Test
    fun `your test case`() {
        // Your test code here
    }
}
```

Example of an integration test:
```kotlin
@Tag("integration")
class YourIntegrationTest {
    @Test
    fun `your test case`() {
        // Your test code here
    }
}
``` 