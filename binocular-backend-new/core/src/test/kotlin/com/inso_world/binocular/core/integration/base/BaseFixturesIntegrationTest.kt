package com.inso_world.binocular.core.integration.base

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.startsWith
import kotlin.text.substring

open class BaseFixturesIntegrationTest : BaseIntegrationTest() {
    companion object {
        const val FIXTURES_PATH = "src/test/resources/fixtures"
        const val SIMPLE_REPO = "simple"
        const val SIMPLE_PROJECT_NAME = "simple"
        const val ADVANCED_REPO = "advanced"
        const val ADVANCED_PROJECT_NAME = "advanced"
        const val OCTO_REPO = "octo"
        const val OCTO_PROJECT_NAME = "octo"

        @kotlin.jvm.JvmStatic
        @BeforeAll
        fun setUp() {
            fun createGitRepo(path: String) {
                val isWindows = java.lang.System.getProperty("os.name").lowercase(java.util.Locale.getDefault()).startsWith("windows")
                val builder = java.lang.ProcessBuilder()
                if (isWindows) {
                    val winPath = java.io.File(FIXTURES_PATH).absolutePath
                    val wslPath = "/mnt/" + winPath[0].lowercase() + winPath.substring(2).replace("\\", "/")
                    kotlin.io.println("WINDOWS: $winPath")
                    kotlin.io.println("WSL: $wslPath")
                    builder.command(
                        "wsl",
                        "bash",
                        "-c",
                        "cd $wslPath && rm -rf $path ${path}_remote.git && ./$path.sh $path",
                    )
                } else {
                    builder.command("sh", "-c", "rm -rf $path ${path}_remote.git && ./$path.sh $path")
                }
                builder.directory(File(FIXTURES_PATH))
                val process = builder.start()
                val streamGobbler: StreamGobbler = StreamGobbler(process.inputStream, System.out::println, path)
                val executorService = Executors.newFixedThreadPool(1)
                val future: Future<*> = executorService.submit(streamGobbler)

                val exitCode = process.waitFor()
                assertDoesNotThrow { future.get(25, TimeUnit.SECONDS) }
                assertEquals(0, exitCode)
            }

            val executorService = Executors.newFixedThreadPool(3)
            val futures =
                listOf(
                    executorService.submit { createGitRepo(SIMPLE_REPO) },
                    executorService.submit { createGitRepo(OCTO_REPO) },
                    executorService.submit { createGitRepo(ADVANCED_REPO) },
                )
            futures.forEach { it.get() }
        }
    }
}
