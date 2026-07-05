package com.inso_world.binocular.web

import com.inso_world.binocular.infrastructure.sql.SqlAppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@SpringBootApplication(
    scanBasePackages = [
        "com.inso_world.binocular.web",
        "com.inso_world.binocular.core.persistence",
        "com.inso_world.binocular.core.service",
        "com.inso_world.binocular.github",
    ],
)
class BinocularWebApplication {

    @Configuration
    @Profile("sql")
    @Import(SqlAppConfig::class)
    class SqlWiring
}

fun main(args: Array<String>) {
    runApplication<BinocularWebApplication>(*args)
}
