package com.inso_world.binocular.cli.integration.shell.base

import com.inso_world.binocular.cli.base.AbstractCliIntegrationTest
import org.springframework.shell.test.autoconfigure.AutoConfigureShell
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient
import org.springframework.test.annotation.DirtiesContext

@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class BaseShellNoDataTest : AbstractCliIntegrationTest()
