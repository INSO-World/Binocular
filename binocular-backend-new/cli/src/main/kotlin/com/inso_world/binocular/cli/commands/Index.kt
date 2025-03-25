package com.inso_world.binocular.cli.commands

import com.inso_world.binocular.cli.index.vcs.VcsIndex
import org.springframework.shell.command.annotation.Command

@Command(command = ["index"])
class Index {

  @Command(command = ["test"])
  fun helloWorld(
  ): String {
    return "Hello, world!"
  }

  @Command(command = ["rust"])
  fun rust(
  ) {
    val idx =
      VcsIndex("/Users/rise/Repositories/Binocular/binocular-backend-new/cli/src/main/mylib/target/aarch64-apple-darwin/debug/libmylib.dylib");
    val result = idx.addNumbers(10, 20)
    println("result: $result")


    println("++++++++++ helloWorld ++++++++++")
    idx.helloWorld()
    println("++++++++++ findRepo ++++++++++")
    idx.findRepo("/Users/rise/Repositories/Binocular/")
  }
}
