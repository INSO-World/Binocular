package com.inso_world.binocular.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception for validation errors in the application.
 * This exception is used when input data fails validation rules.
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Validation error")
class ValidationException : GraphQLException {
    constructor(message: String, extensions: Map<String, Any> = emptyMap()) : super(message, extensions)

    constructor(message: String, cause: Throwable, extensions: Map<String, Any> = emptyMap()) : super(message, cause, extensions)

    constructor(cause: Throwable, extensions: Map<String, Any> = emptyMap()) : super(cause, extensions)
}
