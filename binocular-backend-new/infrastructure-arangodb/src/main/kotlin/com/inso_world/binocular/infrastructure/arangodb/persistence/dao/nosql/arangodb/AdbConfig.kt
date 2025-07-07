package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.arangodb.ArangoDB
import com.arangodb.springframework.annotation.EnableArangoRepositories
import com.arangodb.springframework.config.ArangoConfiguration
import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
@EnableArangoRepositories(
    basePackages = ["com.inso_world.binocular.infrastructure.arangodb.persistence", "com.inso_world.binocular.infrastructure.arangodb.model"],
)
class AdbConfig(
    @Autowired private val infraConfig: InfrastructureConfig,
) : ArangoConfiguration {
    override fun arango(): ArangoDB.Builder {
        var builder =
            ArangoDB
                .Builder()
                .host(infraConfig.database.host, infraConfig.database.port.toInt())

        builder = infraConfig.database.user?.let { builder.user(it) }
        builder = infraConfig.database.password?.let { builder.password(it) }

        return builder
    }

    override fun database(): String = infraConfig.database.databaseName

    override fun returnOriginalEntities(): Boolean = false
}
