package com.inso_world.binocular.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception for GraphQL-specific errors.
 * This exception can include additional metadata that will be included in the GraphQL error response.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "GraphQL error")
open class GraphQLException : ServiceException {
    val extensions: Map<String, Any>

    constructor(message: String, extensions: Map<String, Any> = emptyMap()) : super(message) {
        this.extensions = extensions
    }

    constructor(message: String, cause: Throwable, extensions: Map<String, Any> = emptyMap()) : super(message, cause) {
        this.extensions = extensions
    }

    constructor(cause: Throwable, extensions: Map<String, Any> = emptyMap()) : super(cause) {
        this.extensions = extensions
    }
}
