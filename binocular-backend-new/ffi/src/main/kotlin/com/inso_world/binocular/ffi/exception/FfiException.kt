package com.inso_world.binocular.ffi.exception

import com.inso_world.binocular.core.exception.BinocularIndexerException

internal class FfiException : BinocularIndexerException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
