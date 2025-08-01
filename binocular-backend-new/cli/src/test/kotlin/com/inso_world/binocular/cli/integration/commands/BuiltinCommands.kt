package com.inso_world.binocular.cli.integration.commands

import com.inso_world.binocular.cli.integration.commands.base.BaseShellNoDataTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.test.ShellAssertions
import org.springframework.shell.test.ShellTestClient
import java.util.concurrent.TimeUnit

internal class BuiltinCommands(
    @Autowired val client: ShellTestClient,
) : BaseShellNoDataTest() {
    @Test
    fun context_loads() {
        val session = client.interactive().run()

        await().atMost(2, TimeUnit.SECONDS).untilAsserted({
            ShellAssertions
                .assertThat(session.screen())
                .containsText("shell:>")
        })
    }

    @Test
    fun `help shows AVAILABLE COMMANDS`() {
        val session = client.nonInterative("help").run()

        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            assertAll(
                { ShellAssertions.assertThat(session.screen()).containsText("AVAILABLE COMMANDS") },
                { ShellAssertions.assertThat(session.screen()).containsText("Index Commands") },
                { ShellAssertions.assertThat(session.screen()).containsText("index commits") },
                { ShellAssertions.assertThat(session.screen()).containsText("index blames") },
                { ShellAssertions.assertThat(session.screen()).containsText("index diffs") },
            )
        }
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
        val session = client.interactive().run()

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

        await().atMost(2, TimeUnit.SECONDS).untilAsserted {
            assertAll(
                { ShellAssertions.assertThat(session.screen()).containsText("SYNOPSIS") },
                { ShellAssertions.assertThat(session.screen()).containsText("OPTIONS") },
                { ShellAssertions.assertThat(session.screen()).containsText("$cmdGrp $cmdName -") },
                { ShellAssertions.assertThat(session.screen()).containsText("help for $cmdGrp $cmdName") },
            )
        }
    }
}
