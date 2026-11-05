package com.inso_world.binocular.core.exception

open class BinocularInfrastructureException : BinocularException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
