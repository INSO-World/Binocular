package com.inso_world.binocular.web.config

import com.inso_world.binocular.infrastructure.arangodb.persistence.dao.nosql.arangodb.ArangodbAppConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("nosql", "arangodb")
@ComponentScan("com.inso_world.binocular.infrastructure.arangodb")
@Import(ArangodbAppConfig::class)
class ArangodbConfig
