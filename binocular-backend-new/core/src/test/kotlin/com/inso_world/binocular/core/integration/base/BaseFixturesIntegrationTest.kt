package com.inso_world.binocular.core.integration.base

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

open class BaseFixturesIntegrationTest : BaseIntegrationTest() {
  companion object {
    const val FIXTURES_PATH = "./src/test/resources/fixtures"
    const val SIMPLE_REPO = "simple"
    const val ADVANCED_REPO = "advanced"
    const val OCTO_REPO = "octo"

    @JvmStatic
    @BeforeAll
    fun setUp() {
      fun createGitRepo(path: String) {
        val isWindows = System.getProperty("os.name").lowercase(Locale.getDefault()).startsWith("windows")
        val builder = ProcessBuilder()
        if (isWindows) {
          val winPath = File(FIXTURES_PATH).absolutePath
          val wslPath = "/mnt/" + winPath[0].lowercase() + winPath.substring(2).replace("\\", "/")
          println("WINDOWS: $winPath")
          println("WSL: $wslPath")
          builder.command(
            "wsl",
            "bash",
            "-c",
            "cd $wslPath && rm -rf $path ${path}_remote.git && ./$path.sh $path",
          )
        } else {
          builder.command("sh", "-c", "rm -rf $path ${path}_remote.git && ./$path.sh $path")
        }
        //    builder.directory(File(System.getProperty("user.home")))
        builder.directory(File(FIXTURES_PATH))
        val process = builder.start()
        val streamGobbler: StreamGobbler = StreamGobbler(process.inputStream, System.out::println)
        val executorService = Executors.newFixedThreadPool(1)
        val future: Future<*> = executorService.submit(streamGobbler)

        val exitCode = process.waitFor()
        assertDoesNotThrow { future.get(25, TimeUnit.SECONDS) }
        assertEquals(0, exitCode)
      }

      createGitRepo(SIMPLE_REPO)
      createGitRepo(OCTO_REPO)
      createGitRepo(ADVANCED_REPO)
    }
  }
}
