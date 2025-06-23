package com.inso_world.binocular.ffi.exception

import com.inso_world.binocular.core.exception.BinocularException

class BinocularFfiException : BinocularException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
