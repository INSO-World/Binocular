package com.inso_world.binocular.core.service.exception

import com.inso_world.binocular.core.exception.BinocularInfrastructureException

class NotFoundException : BinocularInfrastructureException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
