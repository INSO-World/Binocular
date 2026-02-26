package com.inso_world.binocular.web.graphql.integration.realdata.base

import com.inso_world.binocular.core.delegates.logger
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig
import com.inso_world.binocular.web.BinocularWebApplication
import com.inso_world.binocular.web.base.AbstractWebIntegrationTest
import com.inso_world.binocular.web.graphql.integration.realdata.base.legacy.LegacyHttpGraphQlClient
import com.inso_world.binocular.web.graphql.integration.realdata.base.spring.SpringTesterGraphQlClient
import io.testcontainers.arangodb.containers.ArangoContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Profiles
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.utility.DockerImageName

@AutoConfigureGraphQlTester
@AutoConfigureMockMvc
@SpringBootTest(
    classes = [BinocularWebApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ContextConfiguration(
    initializers = [
        BaseGraphQlCompatibilityIT.Initializer::class,
    ]
)
internal abstract class BaseGraphQlCompatibilityIT {

    companion object {
        private val adbContainer =
            ArangoContainer(
                DockerImageName.parse("ghcr.io/inso-world/binocular-database:3.12.test-data")
                    .asCompatibleSubstituteFor("arangodb")
            )
                .apply { withExposedPorts(8529) }
                .apply { withoutAuth() }
                .apply { withReuse(true) }

        private const val DEFAULT_TARGET = "spring"
        private const val TARGET_LEGACY = "legacy"
        private const val TARGET_SPRING = "spring"
        private const val DEFAULT_LEGACY_URL = "http://[::1]:8080/graphQl"
        private val logger by logger()
    }

    private class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(ctx: ConfigurableApplicationContext) {
            if (!ctx.environment.acceptsProfiles(Profiles.of("arangodb"))) return
            adbContainer.start()

            TestPropertyValues.of(
                "binocular.arangodb.database.name=binocular-binocular",
                "binocular.arangodb.database.host=${adbContainer.host}",
                "binocular.arangodb.database.port=${adbContainer.firstMappedPort}"
            ).applyTo(ctx.environment)
        }
    }

    @Autowired
    protected lateinit var tester: GraphQlTester

    protected val client: CompatibleGraphQlClient by lazy {
        val target = graphqlTarget()
        log("Running GraphQL tests in mode: $target")

        when (target) {
            TARGET_LEGACY -> createLegacyClient()
            TARGET_SPRING -> SpringTesterGraphQlClient(tester)
            else -> error("Unknown graphql.target: $target (use '$TARGET_LEGACY' or '$TARGET_SPRING')")
        }
    }

    private fun createLegacyClient(): CompatibleGraphQlClient {
        val url = legacyUrl()
        log("Legacy endpoint: $url (override with -Dgraphql.legacy.url)")
        return LegacyHttpGraphQlClient(url)
    }

    private fun graphqlTarget(): String =
        System.getProperty("graphql.target", DEFAULT_TARGET).lowercase()

    private fun legacyUrl(): String =
        System.getProperty("graphql.legacy.url", DEFAULT_LEGACY_URL)

    private fun log(message: String) =
        logger.info("[GRAPHQL-IT] $message")

}
