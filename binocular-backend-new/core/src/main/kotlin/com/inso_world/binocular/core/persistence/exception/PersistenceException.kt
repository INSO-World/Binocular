package com.inso_world.binocular.core.persistence.exception

import com.inso_world.binocular.core.exception.BinocularException

open class PersistenceException : BinocularException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
