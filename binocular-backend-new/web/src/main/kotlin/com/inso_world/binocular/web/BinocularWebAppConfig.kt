package com.inso_world.binocular.web

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@ConfigurationProperties(prefix = "binocular")
class BinocularAppConfig {
    lateinit var database: DatabaseConfig
}

@Profile("arangodb")
class DatabaseConfig(
    val databaseName: String,
    val host: String,
    val port: String,
    var user: String?,
    var password: String?,
)
