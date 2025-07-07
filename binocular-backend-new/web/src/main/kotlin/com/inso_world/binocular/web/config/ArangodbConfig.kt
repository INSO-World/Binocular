package com.inso_world.binocular.web.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("nosql", "arangodb")
@ComponentScan("com.inso_world.binocular.infrastructure.arangodb")
class ArangodbConfig
