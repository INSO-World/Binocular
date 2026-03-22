package com.inso_world.binocular.cli.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.sql.SqlTestConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ContextConfiguration(
    initializers = [
        SqlTestConfig.Initializer::class,
    ]
)
internal abstract class AbstractCliIntegrationTest : BaseIntegrationTest()
