package com.inso_world.binocular.cli.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(
    basePackages = [
        "com.inso_world.binocular.core.persistence",
        "com.inso_world.binocular.infrastructure.sql.persistence",
    ],
)
@EntityScan(
    "com.inso_world.binocular.infrastructure.sql.persistence.entity",
    "com.inso_world.binocular.cli.entity",
)
@Configuration
@Profile("sql", "postgres")
@ComponentScan("com.inso_world.binocular.infrastructure.sql")
class SqlConfig
