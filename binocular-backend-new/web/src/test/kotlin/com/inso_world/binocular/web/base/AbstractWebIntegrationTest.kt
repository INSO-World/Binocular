package com.inso_world.binocular.web.base

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.web.BinocularWebApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
    classes = [BinocularWebApplication::class],
)
@ContextConfiguration(
    initializers = [
        com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig.Initializer::class,
    ]
)
internal abstract class AbstractWebIntegrationTest : BaseIntegrationTest()
