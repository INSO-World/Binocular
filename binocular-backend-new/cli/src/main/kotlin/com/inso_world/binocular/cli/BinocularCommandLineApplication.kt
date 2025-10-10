package com.inso_world.binocular.cli

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.shell.command.annotation.CommandScan

@SpringBootApplication(
//    scanBasePackages = [
//        "com.inso_world.binocular.cli",
// //        make sure the ones below match the ones in WebApplication (and vice versa)
//        "com.inso_world.binocular.core.persistence",
//        "com.inso_world.binocular.core.service",
//    ],
)
@CommandScan(basePackages = ["com.inso_world.binocular.cli.commands"])
@ComponentScan(
    basePackages = [
        "com.inso_world.binocular.cli",
//        make sure the ones below match the ones in WebApplication (and vice versa)
        "com.inso_world.binocular.core.persistence",
        "com.inso_world.binocular.core.service",
    ],
)
class BinocularCommandLineApplication {
    companion object {
        private var logger: Logger = LoggerFactory.getLogger(BinocularCommandLineApplication::class.java)
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
    var webType = WebApplicationType.NONE

    val initializer =
        ApplicationContextInitializer<ConfigurableApplicationContext> { ctx ->
            val env = ctx.environment
            if (env.activeProfiles.contains("h2")) {
                webType = WebApplicationType.SERVLET
            }
        }

    SpringApplicationBuilder(BinocularCommandLineApplication::class.java)
        .initializers(initializer)
        .web(webType)
        .run(*args)
}
