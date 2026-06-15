package com.inso_world.binocular.web.base

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.web.BinocularWebApplication
import org.junit.jupiter.api.Tag
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
    classes = [BinocularWebApplication::class],
)
@AutoConfigureGraphQlTester
@ContextConfiguration(
    initializers = [
        com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig.Initializer::class,
    ]
)
@Tag("web")
internal abstract class AbstractWebIntegrationTest : BaseIntegrationTest()
