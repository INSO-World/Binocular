package com.inso_world.binocular.cli.config

import com.inso_world.binocular.infrastructure.sql.SqlAppConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("sql", "postgres")
@Import(SqlAppConfig::class)
internal class SqlConfig
