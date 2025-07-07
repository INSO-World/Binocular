package com.inso_world.binocular.cli

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "binocular")
class BinocularCliConfiguration {
    lateinit var index: IndexConfig
    lateinit var archive: ArchiveConfig
}

class IndexConfig(
    val path: String,
)

class ArchiveConfig(
    val path: String? = null,
)
