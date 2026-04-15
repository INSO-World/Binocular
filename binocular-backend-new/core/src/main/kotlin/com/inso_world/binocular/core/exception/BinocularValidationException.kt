package com.inso_world.binocular.core.exception

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException

class BinocularValidationException : ConstraintViolationException {
    constructor(p0: String, p1: Set<ConstraintViolation<*>>) : super(p0, p1)
    constructor(p0: Set<ConstraintViolation<*>>) : super(p0)

//    constructor(message: String, cause: Throwable) : super(message, cause)
//    constructor(message: String) : super(message)
//    constructor(cause: Throwable) : super(cause)
}
