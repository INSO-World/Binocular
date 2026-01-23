package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.arangodb.ArangoDB
import com.arangodb.springframework.annotation.EnableArangoRepositories
import com.arangodb.springframework.config.ArangoConfiguration
import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableArangoRepositories(
    basePackages = ["com.inso_world.binocular.infrastructure.arangodb.persistence", "com.inso_world.binocular.infrastructure.arangodb.model"],
)
class ArangodbAppConfig(
    @Autowired private val infraConfig: InfrastructureConfig,
) : ArangoConfiguration {
    override fun arango(): ArangoDB.Builder {
        var builder =
            ArangoDB
                .Builder()
                .host(infraConfig.arangodb.database.host, infraConfig.arangodb.database.port.toInt())

        builder = infraConfig.arangodb.database.user?.let { builder.user(it) }
        builder = infraConfig.arangodb.database.password?.let { builder.password(it) }

        return builder
    }

    override fun database(): String = infraConfig.arangodb.database.name

    override fun returnOriginalEntities(): Boolean = false
}
