package com.inso_world.binocular.cli.config

data class ExportSelectionConfig (
    val includePrefixes: List<String> = listOf("src/"),
    val excludePrefixes: List<String> = emptyList(),
    val maxFiles: Int = 5000,
    val maxBlobBytes: Long = 200_000,
    val skipBinary: Boolean = true
)
