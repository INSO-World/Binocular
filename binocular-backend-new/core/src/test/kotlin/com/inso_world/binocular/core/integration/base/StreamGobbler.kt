package com.inso_world.binocular.core.integration.base

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

internal class StreamGobbler(
  private var inputStream: InputStream,
  private var consumer: Consumer<String>,
) : Runnable {
  override fun run() {
    BufferedReader(InputStreamReader(inputStream))
      .lines()
      .forEach(consumer)
  }
}
