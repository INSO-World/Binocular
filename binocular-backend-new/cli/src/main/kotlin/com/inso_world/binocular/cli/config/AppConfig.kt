package com.inso_world.binocular.cli.config

import com.inso_world.binocular.core.BinocularConfig
import org.springframework.context.annotation.Configuration

@Configuration
internal class BinocularConfiguration : BinocularConfig() {
    lateinit var cli: CliConfig
}

class CliConfig(
    val index: IndexConfig
)

class ScmConfig(
    val path: String
)

class IndexConfig(
    val scm: ScmConfig
)
