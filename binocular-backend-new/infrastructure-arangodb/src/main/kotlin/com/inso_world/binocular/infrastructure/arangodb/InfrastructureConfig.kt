package com.inso_world.binocular.infrastructure.arangodb

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "binocular")
@ComponentScan("com.inso_world.binocular.infrastructure.arangodb")
class InfrastructureConfig {
    lateinit var database: DatabaseConfig
}

class DatabaseConfig(
    val databaseName: String,
    val host: String,
    val port: String,
    var user: String?,
    var password: String?,
)
