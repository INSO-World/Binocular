package com.inso_world.binocular.cli.config

import org.yaml.snakeyaml.Yaml

object ExportConfigLoader {

    fun loadDefaultPolicy(): ExportSelectionConfig {
        val stream = ExportConfigLoader::class.java.classLoader
            .getResourceAsStream("export-policy.yaml")
            ?: return ExportSelectionConfig()

        stream.use {
            val root = Yaml().load<Map<String, Any>>(it) ?: return ExportSelectionConfig()
            val selection = (root["selection"] as? Map<*, *>) ?: emptyMap<Any, Any>()

            val include = (selection["includePrefixes"] as? List<*>)?.mapNotNull { v -> v as? String }
                ?: listOf("src/")
            val exclude = (selection["excludePrefixes"] as? List<*>)?.mapNotNull { v -> v as? String }
                ?: emptyList()
            val maxFiles = (selection["maxFiles"] as? Number)?.toInt() ?: 5000
            val maxBlobBytes = (selection["maxBlobBytes"] as? Number)?.toLong() ?: 200_000L
            val skipBinary = (selection["skipBinary"] as? Boolean) ?: true

            return ExportSelectionConfig(
                includePrefixes = include,
                excludePrefixes = exclude,
                maxFiles = maxFiles,
                maxBlobBytes = maxBlobBytes,
                skipBinary = skipBinary
            )
        }
    }

    fun exportAllConfig(): ExportSelectionConfig =
        ExportSelectionConfig(
            includePrefixes = emptyList(),   // empty => include everything
            excludePrefixes = emptyList(),   // exclude nothing
            maxFiles = Int.MAX_VALUE         // no limit
        )
}
