package com.inso_world.binocular.web.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "instance not found")
class NotFoundException : ServiceException {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}
