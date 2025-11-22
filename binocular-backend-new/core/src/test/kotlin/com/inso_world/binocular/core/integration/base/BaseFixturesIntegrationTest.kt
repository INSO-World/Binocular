package com.inso_world.binocular.core.integration.base

import com.inso_world.binocular.core.delegates.logger
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.toPath
import kotlin.io.path.walk

open class BaseFixturesIntegrationTest : BaseIntegrationTest() {
    companion object {
        val logger by logger()

        /**
         * Absolute path to the fixtures directory.
         *
         * This path is resolved from the classpath to work correctly when:
         * - Running tests directly in the core module (filesystem path)
         * - Running tests in other modules (e.g., ffi) that depend on core's test-jar (JAR path)
         *
         * When fixtures are in a JAR, they are extracted to a temporary directory since
         * the shell scripts need to be executable on the filesystem.
         */
        val FIXTURES_PATH: String by lazy {
            val resource = BaseFixturesIntegrationTest::class.java.getResource("/fixtures")
                ?: throw IllegalStateException(
                    "Cannot find fixtures directory on classpath. " +
                            "Ensure core module's test resources are available."
                )

            try {
                val uri = resource.toURI()

                // Check if fixtures are in a JAR (e.g., when accessed via test-jar dependency)
                if (uri.scheme == "jar") {
                    logger.info("Fixtures are in JAR, extracting to temporary directory")

                    // Extract fixtures from JAR to temp directory
                    val tempDir = Files.createTempDirectory("binocular-fixtures-")
                    Runtime.getRuntime().addShutdownHook(Thread {
                        tempDir.toFile().deleteRecursively()
                    })

                    // Create filesystem for the JAR
                    val fs = try {
                        FileSystems.getFileSystem(uri)
                    } catch (e: java.nio.file.FileSystemNotFoundException) {
                        FileSystems.newFileSystem(uri, emptyMap<String, Any>())
                    }

                    val fixturesInJar = fs.getPath("/fixtures")

                    // Copy all fixtures to temp directory
                    fixturesInJar.walk().forEach { source ->
                        val relativePath = fixturesInJar.relativize(source).toString()
                        if (relativePath.isNotEmpty()) {
                            val target = tempDir.resolve(relativePath)
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(target)
                            } else {
                                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
                                // Make shell scripts executable
                                if (target.fileName.toString().endsWith(".sh")) {
                                    target.toFile().setExecutable(true)
                                }
                            }
                        }
                    }

                    logger.info("Fixtures extracted to: ${tempDir.absolutePathString()}")
                    tempDir.absolutePathString()
                } else {
                    // Running tests directly in core module
                    uri.toPath().absolutePathString()
                }
            } catch (e: Exception) {
                logger.error("Failed to resolve fixtures path from: ${resource.path}", e)
                throw RuntimeException("Failed to resolve fixtures path", e)
            }
        }

        const val SIMPLE_REPO = "simple"
        const val SIMPLE_PROJECT_NAME = "simple"
        const val ADVANCED_REPO = "advanced"
        const val ADVANCED_PROJECT_NAME = "advanced"
        const val OCTO_REPO = "octo"
        const val OCTO_PROJECT_NAME = "octo"

        fun execCmd(path: String, vararg cmd: String) {
            val isWindows =
                java.lang.System.getProperty("os.name").lowercase(java.util.Locale.getDefault()).startsWith("windows")
            val builder = java.lang.ProcessBuilder()
            if (isWindows) {
                builder.command(*cmd)
            } else {
                builder.command(*cmd)
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

        @kotlin.jvm.JvmStatic
        @BeforeAll
        fun setUp() {
            fun createGitRepo(path: String) {
                val isWindows = java.lang.System.getProperty("os.name").lowercase(java.util.Locale.getDefault())
                    .startsWith("windows")
                if (isWindows) {
                    val winPath = java.io.File(FIXTURES_PATH).absolutePath
                    val wslPath = "/mnt/" + winPath[0].lowercase() + winPath.substring(2).replace("\\", "/")
                    execCmd(
                        path = path,
                        "wsl",
                        "bash",
                        "-c",
                        "cd $wslPath && rm -rf $path ${path}_remote.git && ./$path.sh $path",
                    )
                } else {
                    execCmd(path = path, "sh", "-c", "rm -rf $path ${path}_remote.git && ./$path.sh $path")
                }
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

    fun addCommit() {
        val executorService = Executors.newFixedThreadPool(3)
        SIMPLE_REPO.let { path ->
            val future = executorService.submit {
                execCmd(path = path, "sh", "-c", "./${path}_add_commit.sh $path")
            }
            future.get()
        }
    }
}
