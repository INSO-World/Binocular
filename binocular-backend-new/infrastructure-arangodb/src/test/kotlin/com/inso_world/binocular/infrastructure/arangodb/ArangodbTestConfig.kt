package com.inso_world.binocular.infrastructure.arangodb

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.ArangodbAppConfig
import io.testcontainers.arangodb.containers.ArangoContainer
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Profiles
import org.springframework.test.context.ContextConfiguration

@Configuration
@ComponentScan("com.inso_world.binocular.infrastructure.arangodb")
@ContextConfiguration(initializers = [ArangodbTestConfig.Initializer::class])
@Import(ArangodbAppConfig::class)
class ArangodbTestConfig {
    companion object {
        val adbContainer =
            ArangoContainer("arangodb:3.12")
                .apply { withExposedPorts(8529) }
                .apply { withoutAuth() }
                .apply { withReuse(true) }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(ctx: ConfigurableApplicationContext) {
            if (!ctx.environment.acceptsProfiles(Profiles.of("arangodb"))) return
            adbContainer.start()

            TestPropertyValues.of(
                "binocular.arangodb.database.name=infrastructure_arangodb_it",
                "binocular.arangodb.database.host=${adbContainer.host}",
                "binocular.arangodb.database.port=${adbContainer.firstMappedPort}"
            ).applyTo(ctx.environment)
        }
    }
}
