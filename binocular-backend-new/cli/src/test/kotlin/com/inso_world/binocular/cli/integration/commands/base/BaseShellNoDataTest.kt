package com.inso_world.binocular.cli.integration.commands.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.shell.test.autoconfigure.AutoConfigureShell
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient
import org.springframework.test.annotation.DirtiesContext

@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
class BaseShellNoDataTest : BaseIntegrationTest()
