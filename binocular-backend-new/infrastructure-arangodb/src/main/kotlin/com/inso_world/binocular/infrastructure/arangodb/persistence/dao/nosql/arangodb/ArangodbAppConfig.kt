package com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb

import com.arangodb.ArangoDB
import com.arangodb.springframework.annotation.EnableArangoRepositories
import com.arangodb.springframework.config.ArangoConfiguration
import com.arangodb.springframework.core.ArangoOperations
import com.arangodb.springframework.core.template.ArangoTemplate
import com.inso_world.binocular.infrastructure.arangodb.InfrastructureConfig
import com.inso_world.binocular.infrastructure.arangodb.persistence.converter.LocalDateTimeToStringConverter
import com.inso_world.binocular.infrastructure.arangodb.persistence.converter.StringToLocalDateTimeConverter
import com.inso_world.binocular.infrastructure.arangodb.persistence.converter.StringToUuidConverter
import com.inso_world.binocular.infrastructure.arangodb.persistence.converter.UuidToStringConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
@EnableArangoRepositories(
    basePackages = [
        "com.inso_world.binocular.infrastructure.arangodb.persistence",
        "com.inso_world.binocular.infrastructure.arangodb.model",
    ],
)
class ArangodbAppConfig(
    @Autowired private val infraConfig: InfrastructureConfig,
) : ArangoConfiguration {
    override fun arango(): ArangoDB.Builder {
        var builder =
            ArangoDB
                .Builder()
                .host(
                    infraConfig.arangodb.database.host,
                    infraConfig.arangodb.database.port
                        .toInt(),
                )

        builder =
            infraConfig.arangodb.database.user
                ?.let { builder.user(it) }
        builder =
            infraConfig.arangodb.database.password
                ?.let { builder.password(it) }

        return builder
    }

    override fun database(): String = infraConfig.arangodb.database.name

    override fun returnOriginalEntities(): Boolean = false

    override fun customConverters() =
        listOf(
            UuidToStringConverter(),
            StringToUuidConverter(),
            LocalDateTimeToStringConverter(),
            StringToLocalDateTimeConverter(),
        )

    /**
     * Overrides the default [ArangoOperations] bean to ensure migrations
     * complete before Spring Data ArangoDB initializes repositories and
     * creates collection indexes.
     *
     * Without this, `@PersistentIndexed(unique = true)` on entity fields
     * like `iid` triggers index creation during repository bean setup,
     * which fails if existing documents haven't been backfilled yet.
     */
    @Bean
    @DependsOn("migrationRunner")
    override fun arangoTemplate(): ArangoTemplate = super.arangoTemplate()
}
