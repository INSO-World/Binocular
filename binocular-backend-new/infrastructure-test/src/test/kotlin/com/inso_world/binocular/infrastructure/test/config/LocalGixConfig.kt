package com.inso_world.binocular.infrastructure.test.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("gix")
@ComponentScan("com.inso_world.binocular.ffi")
internal class LocalGixConfig
