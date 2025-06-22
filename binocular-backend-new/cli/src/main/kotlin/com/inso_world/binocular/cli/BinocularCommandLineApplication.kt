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
