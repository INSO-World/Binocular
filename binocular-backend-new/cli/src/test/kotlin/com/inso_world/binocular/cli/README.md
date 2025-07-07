# Test Structure

This directory contains all tests for the Binocular CLI application. The tests are organized into two main categories:

## Unit Tests (`unit/`)

Unit tests are located in the `unit/` directory and focus on testing individual components in isolation. These tests:

- Run quickly
- Don't require external dependencies
- Use mocks and stubs where appropriate
- Test a single unit of functionality

Structure:

```
unit/
├── mapper/         # Tests for data mappers
├── service/        # Tests for service layer
├── utils/          # Tests for utility functions
└── commands/       # Tests for command implementations
```

## Integration Tests (`integration/`)

Integration tests are located in the `integration/` directory and focus on testing how components work together. These
tests:

- May require external dependencies
- Test multiple components working together
- May interact with databases or external services
- Take longer to run

Structure:

```
integration/
├── api/           # Tests for API integration
├── persistence/   # Tests for database integration
├── uniffi/        # Tests for FFI integration
└── e2e/          # End-to-end tests
```

## Test Resources

Test resources are organized in the `resources/` directory:

```
resources/
├── unit/         # Resources for unit tests
└── integration/  # Resources for integration tests
```

## Base Test Classes

- `BaseUnitTest.kt`: Base class for unit tests
- `BaseIntegrationTest.kt`: Base class for integration tests
- `BaseFixturesIntegrationTest.kt`: Base class for integration tests with fixtures

## Running Tests

### Unit Tests

```bash
# Run all unit tests
mvn test

# Run specific unit test class
mvn test -Dtest=YourUnitTest

# Run specific unit test method
mvn test -Dtest=YourUnitTest#testMethod
```

### Integration Tests

```bash
# Run all integration tests
mvn verify

# Run specific integration test class
mvn verify -Dtest=YourIntegrationTest

# Run specific integration test method
mvn verify -Dtest=YourIntegrationTest#testMethod
```

### Test Coverage

```bash
# Generate test coverage report
mvn verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Common Maven Test Options

```bash
# Skip tests
mvn clean install -DskipTests

# Skip unit tests but run integration tests
mvn verify -DskipUnitTests

# Run tests in parallel
mvn test -Dparallel=true

# Set test timeout
mvn test -Dtest.timeout=30
```

### Test Profiles

```bash
# Run tests with specific profile
mvn test -P unit-tests
mvn verify -P integration-tests

# Run tests with multiple profiles
mvn verify -P unit-tests,integration-tests
``` 
