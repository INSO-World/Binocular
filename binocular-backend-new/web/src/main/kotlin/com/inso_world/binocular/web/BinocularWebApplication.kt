package com.inso_world.binocular.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(exclude = [
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration::class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration::class
])
class BinocularWebApplication

fun main(args: Array<String>) {
  runApplication<BinocularWebApplication>(*args)
}
