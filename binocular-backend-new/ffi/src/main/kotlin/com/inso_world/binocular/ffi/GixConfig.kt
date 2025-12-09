package com.inso_world.binocular.ffi

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "binocular")
internal open class BinocularConfig {
    lateinit var gix: GixConfig
}

class GixConfig(
    val skipMerges: Boolean,
    val useMailmap: Boolean
)
