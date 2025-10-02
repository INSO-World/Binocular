package com.inso_world.binocular.core.exception

abstract class BinocularIndexerException : BinocularException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
