package com.inso_world.binocular.web.graphql.error

import com.inso_world.binocular.web.exception.GraphQLException
import com.inso_world.binocular.web.exception.NotFoundException
import com.inso_world.binocular.web.exception.ServiceException
import com.inso_world.binocular.web.exception.ValidationException
import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component

/**
 * Global exception handler for GraphQL queries and mutations.
 * This handler converts exceptions to GraphQL errors with appropriate error types and extensions.
 */
@Component
class GraphQLExceptionHandler : DataFetcherExceptionResolverAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(GraphQLExceptionHandler::class.java)

    public override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        logger.error("GraphQL error: ${ex.message}", ex)

        return when (ex) {
            is ValidationException -> GraphqlErrorBuilder.newError()
                .message(ex.message)
                .errorType(ErrorType.ValidationError)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .extensions(ex.extensions + mapOf("code" to "VALIDATION_ERROR"))
                .build()

            is GraphQLException -> GraphqlErrorBuilder.newError()
                .message(ex.message)
                .errorType(ErrorType.ValidationError)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .extensions(ex.extensions)
                .build()

            is NotFoundException -> GraphqlErrorBuilder.newError()
                .message(ex.message ?: "Resource not found")
                .errorType(ErrorType.DataFetchingException)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .extensions(mapOf("code" to "NOT_FOUND"))
                .build()

            is ServiceException -> GraphqlErrorBuilder.newError()
                .message(ex.message ?: "Service error")
                .errorType(ErrorType.DataFetchingException)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .extensions(mapOf("code" to "SERVICE_ERROR"))
                .build()

            else -> GraphqlErrorBuilder.newError()
                .message("Internal server error")
                .errorType(ErrorType.ExecutionAborted)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .extensions(mapOf("code" to "INTERNAL_ERROR"))
                .build()
        }
    }
}
