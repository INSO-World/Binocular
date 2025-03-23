package com.inso_world.binocular.web.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/hello"])
class HelloWorld {
  @GetMapping
  fun findAll(): String {
    return "Hello World"
  }
}
