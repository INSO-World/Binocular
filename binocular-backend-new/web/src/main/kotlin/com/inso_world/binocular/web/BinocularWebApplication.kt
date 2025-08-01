package com.inso_world.binocular.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.inso_world.binocular.web",
//        make sure the ones below match the ones in CliApplication (and vice versa)
        "com.inso_world.binocular.core.persistence",
        "com.inso_world.binocular.core.service",
    ],
)
class BinocularWebApplication

fun main(args: Array<String>) {
    runApplication<BinocularWebApplication>(*args)
}
