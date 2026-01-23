package com.inso_world.binocular.infrastructure.arangodb

import com.inso_world.binocular.core.BinocularConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.inso_world.binocular.infrastructure.arangodb")
class InfrastructureConfig : BinocularConfig() {
    lateinit var arangodb: AdbConfig
}

class AdbConfig {
    lateinit var database: DatabaseConfig
}

class DatabaseConfig(
    val name: String,
    val host: String,
    val port: String,
    var user: String?,
    var password: String?,
)
