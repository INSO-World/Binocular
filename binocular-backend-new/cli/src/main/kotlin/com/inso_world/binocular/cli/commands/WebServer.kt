package com.inso_world.binocular.cli

import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle
import org.springframework.context.annotation.Bean
import org.springframework.shell.command.annotation.Command
import org.springframework.shell.jline.PromptProvider


@Command(command = ["server"])
class WebServer {

  @Command(command = ["start"])
  fun start(): String {
//    Thread {
//      SpringApplicationBuilder(BinocularWebApplication::class.java)
//        .web(WebApplicationType.SERVLET)  // Enable the web environment
//        .run()
//    }.start()
    return "Web server started."
  }
}
