package com.inso_world.binocular.cli

import com.inso_world.binocular.cli.index.vcs.VcsIndex
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.shell.command.annotation.CommandScan
import org.springframework.shell.jline.PromptProvider

@SpringBootApplication
@CommandScan(basePackages = ["com.inso_world.binocular.cli"])
class BinocularCommandLineApplication

fun main(args: Array<String>) {
//  println(System.getProperty("java.library.path"))
//   Start Spring Boot in CLI mode (no web server)
  SpringApplicationBuilder(BinocularCommandLineApplication::class.java)
    .web(WebApplicationType.NONE)
    .run(*args)
}

@Bean
fun myPromptProvider(): PromptProvider {
  return PromptProvider { AttributedString("my-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)) }
}
