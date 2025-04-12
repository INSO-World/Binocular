package com.inso_world.binocular.cli

import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.shell.command.annotation.CommandScan
import org.springframework.shell.jline.PromptProvider

@SpringBootApplication
@CommandScan(basePackages = ["com.inso_world.binocular.cli.commands"])
//@ComponentScan(basePackages = ["com.inso_world.binocular"])
class BinocularCommandLineApplication {
  @Bean
  fun myPromptProvider(): PromptProvider {
    return PromptProvider { AttributedString("binocular-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)) }
  }
}

fun main(args: Array<String>) {
  SpringApplicationBuilder(BinocularCommandLineApplication::class.java)
    .web(WebApplicationType.NONE)
    .run(*args)
}


