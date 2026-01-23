package com.inso_world.binocular.ffi

import com.inso_world.binocular.core.BinocularConfig
import org.springframework.context.annotation.Configuration

@Configuration
internal open class FfiConfig : BinocularConfig() {
    lateinit var gix: GixConfig
}

class GixConfig(
    val skipMerges: Boolean,
    val useMailmap: Boolean
)
