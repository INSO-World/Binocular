# GraphQL Error Handling

This document describes the error handling approach for the GraphQL API in the Binocular project.

## Overview

The error handling mechanism is designed to:
1. Provide consistent error responses across the API
2. Include useful information in error messages
3. Map exceptions to appropriate GraphQL error types
4. Log errors for debugging purposes

## Components

### Exception Classes

- **BinocularException**: Base exception class for all exceptions in the project
- **ServiceException**: Base exception class for service-layer exceptions
- **NotFoundException**: Exception for when an entity is not found
- **GraphQLException**: Exception for GraphQL-specific errors, with support for extensions
- **ValidationException**: Exception for validation errors, extends GraphQLException

### Error Handler

The `GraphQLExceptionHandler` class is a global exception handler for GraphQL queries and mutations. It converts exceptions to GraphQL errors with appropriate error types and extensions.

### Utility Methods

The `GraphQLValidationUtils` class provides utility methods for validation and precondition checking:
- `validatePagination`: Validates pagination parameters
- `requireEntityExists`: Checks if an entity exists and throws a NotFoundException if it doesn't

## Usage

### In Controllers

```kotlin
@Controller
@SchemaMapping(typeName = "Commit")
class CommitController(
  @Autowired private val commitService: CommitService,
) {
  @QueryMapping(name = "commits")
  fun findAll(@Argument page: Int?, @Argument perPage: Int?): Iterable<Commit> {
    GraphQLValidationUtils.validatePagination(page, perPage)

    return commitService.findAll(page, perPage)
  }

  @QueryMapping(name = "commit")
  fun findById(@Argument id: String): Commit {
    return GraphQLValidationUtils.requireEntityExists(commitService.findById(id), "Commit", id)
  }
}
```

### Throwing Custom Exceptions

```kotlin
// For validation errors
throw ValidationException("Invalid input", mapOf("field" to "email", "reason" to "Invalid format"))

// For not found errors
throw NotFoundException("User not found with id: $id")

// For service errors
throw ServiceException("Failed to process request")

// For other GraphQL-specific errors
throw GraphQLException("Custom GraphQL error", mapOf("custom" to "metadata"))
```

## Error Response Format

GraphQL errors are returned in the following format:

```json
{
  "errors": [
    {
      "message": "Resource not found",
      "locations": [
        {
          "line": 2,
          "column": 3
        }
      ],
      "path": ["commit", "123"],
      "extensions": {
        "code": "NOT_FOUND"
      }
    }
  ],
  "data": null
}
```

## Error Types

- **ValidationError**: For input validation errors
- **DataFetchingException**: For errors that occur while fetching data
- **ExecutionAborted**: For unexpected errors that abort execution

## Error Codes

- **VALIDATION_ERROR**: The input data failed validation
- **NOT_FOUND**: The requested resource was not found
- **SERVICE_ERROR**: An error occurred in the service layer
- **INTERNAL_ERROR**: An unexpected internal error occurred
- Custom codes can be included in the extensions map of GraphQLException or ValidationException
