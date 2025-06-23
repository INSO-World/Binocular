package com.inso_world.binocular.core.persistence.exception

class NotFoundException : PersistenceException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
