package com.inso_world.binocular.cli.integration.shell

import com.inso_world.binocular.cli.integration.shell.base.BaseShellWithDataTest
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.test.ShellAssertions
import org.springframework.shell.test.ShellTestClient
import java.util.concurrent.TimeUnit

internal class BinocularCommands : BaseShellWithDataTest() {

    @Autowired
    private lateinit var client: ShellTestClient

    private lateinit var session: ShellTestClient.InteractiveShellSession

    @BeforeEach
    fun beforeEach() {
        session = client.interactive().run()
    }

    @Test
    fun `session running`() {
        assertFalse(session.isComplete)
    }

    @ParameterizedTest
    @CsvSource(
        "index,commits",
        "index,diffs",
        "index,blames",
        "index,hello",
    )
    fun `help shows USAGE - index commits`(
        cmdGrp: String,
        cmdName: String,
    ) {
        session.write(
            session
                .writeSequence()
                .text("help")
                .space()
                .text(cmdGrp)
                .space()
                .text(cmdName)
                .space()
                .carriageReturn()
                .build(),
        )

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            assertAll(
                { ShellAssertions.assertThat(session.screen()).containsText("SYNOPSIS") },
                { ShellAssertions.assertThat(session.screen()).containsText("OPTIONS") },
                { ShellAssertions.assertThat(session.screen()).containsText("$cmdGrp $cmdName -") },
                { ShellAssertions.assertThat(session.screen()).containsText("help for $cmdGrp $cmdName") },
            )
        }
    }
}
