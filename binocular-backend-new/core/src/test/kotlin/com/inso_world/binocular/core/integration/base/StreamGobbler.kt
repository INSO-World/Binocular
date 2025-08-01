package com.inso_world.binocular.core.integration.base

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

internal class StreamGobbler(
    private var inputStream: InputStream,
    private var consumer: Consumer<String>,
    private val path: String,
) : Runnable {
    override fun run() {
        val fixedWidth = 12
        val formattedPath = "[$path]"
        val paddedPath =
            if (formattedPath.length > fixedWidth) {
                formattedPath.take(fixedWidth)
            } else {
                formattedPath.padEnd(fixedWidth, ' ')
            }
        BufferedReader(InputStreamReader(inputStream))
            .lines()
            .forEach { line -> consumer.accept("$paddedPath\t$line") }
    }
}
