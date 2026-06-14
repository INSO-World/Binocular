package com.inso_world.binocular.github.exception

import com.inso_world.binocular.core.exception.BinocularException

class ServiceException : BinocularException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
