package com.inso_world.binocular.infrastructure.test.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Profile("postgres")
@Import(com.inso_world.binocular.infrastructure.sql.SqlTestConfig::class)
@Configuration
internal class LocalPostgresConfig
