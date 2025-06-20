package com.inso_world.binocular.cli

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.shell.command.annotation.CommandScan


@SpringBootApplication
@CommandScan(basePackages = ["com.inso_world.binocular.cli.commands"])
@EnableJpaRepositories(basePackages = ["com.inso_world.binocular.core.persistence", "com.inso_world.binocular.cli.persistence"])
class BinocularCommandLineApplication {
  private var logger: Logger = LoggerFactory.getLogger(BinocularCommandLineApplication::class.java)

  init {
    logger.info("Loading native libraries...")
    val rp = loadPlatformLibrary("binocular_ffi")
    logger.debug("Loaded library: $rp")
    logger.info("Library loaded successfully.")
  }
//  @Bean
//  fun myPromptProvider(): PromptProvider {
//    return PromptProvider {
//      AttributedString(
//        "binocular-shell:>",
//        AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
//      )
//    }
//  }
}

fun main(args: Array<String>) {
  SpringApplicationBuilder(BinocularCommandLineApplication::class.java)
//    .web(WebApplicationType.NONE)
    .run(*args)
}

@Throws(UnsupportedOperationException::class)
private fun loadPlatformLibrary(libBaseName: String): String {
  // 1) Detect platform
  val platform = detectPlatform()

  // 2) Map the name to e.g. "libfoo.so" / "foo.dll" / "libfoo.dylib"
  val mappedName = System.mapLibraryName(libBaseName)


  // 3) Build resource path under /{platform}/{mappedName}
  val resourcePath = "/$platform/$mappedName"

  System.setProperty("uniffi.component.$libBaseName.libraryOverride", resourcePath)
  return resourcePath
}

@Throws(UnsupportedOperationException::class)
private fun detectPlatform(): String {
  val os = System.getProperty("os.name").lowercase()
  val arch = System.getProperty("os.arch").lowercase()

  return when {
    // macOS
    os.contains("mac") && (arch == "x86_64" || arch == "amd64") -> "x86_64-apple-darwin"
    os.contains("mac") && (arch == "aarch64" || arch == "arm64") -> "aarch64-apple-darwin"

    // Linux
    (os.contains("nux") || os.contains("nix")) && (arch == "x86_64" || arch == "amd64") -> "x86_64-unknown-linux-gnu"
    (os.contains("nux") || os.contains("nix")) && arch == "aarch64" -> "aarch64-unknown-linux-gnu"

    // Windows
    os.contains("win") && (arch == "x86_64" || arch == "amd64") -> "x86_64-pc-windows-msvc"
    os.contains("win") && arch == "aarch64" -> "aarch64-pc-windows-msvc"

    else -> throw UnsupportedOperationException("Unsupported OS/Arch combination: $os/$arch")
  }
}
