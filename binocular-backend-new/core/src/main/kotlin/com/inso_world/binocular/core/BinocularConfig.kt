package com.inso_world.binocular.core

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "binocular")
open class BinocularConfig
