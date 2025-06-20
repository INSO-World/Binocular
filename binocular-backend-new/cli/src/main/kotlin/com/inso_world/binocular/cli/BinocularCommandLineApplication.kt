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
  var arch = System.getProperty("os.arch").lowercase()
  val osPart = if (os.contains("win")) "windows"
  else if (os.contains("mac")) "apple-darwin"
  else if (os.contains("nux") || os.contains("nix")) "linux"
  else throw UnsupportedOperationException("Unsupported OS: $os")
  if (arch == "amd64" || arch == "x86_64") arch = "x86_64"
  else if (arch == "aarch64") arch = "aarch64"
  return "$arch-$osPart"
}
