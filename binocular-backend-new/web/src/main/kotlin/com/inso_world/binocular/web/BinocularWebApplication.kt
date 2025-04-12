package com.inso_world.binocular.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class BinocularWebApplication

fun main(args: Array<String>) {
  runApplication<BinocularWebApplication>(*args)
}
