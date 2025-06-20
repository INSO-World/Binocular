package com.inso_world.binocular.cli.exception

import com.inso_world.binocular.core.exception.BinocularException

class PersistenceException : BinocularException {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}
