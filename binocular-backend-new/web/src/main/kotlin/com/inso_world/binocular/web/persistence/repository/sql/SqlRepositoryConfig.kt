package com.inso_world.binocular.web.persistence.repository.sql

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Configuration class to enable JPA repositories for SQL entities.
 * This class is only active when the "sql" profile is active.
 */
@Configuration
@Profile("sql")
@EnableJpaRepositories(
    basePackages = ["com.inso_world.binocular.web.persistence.repository.sql"],
    repositoryBaseClass = org.springframework.data.jpa.repository.support.SimpleJpaRepository::class
)
class SqlRepositoryConfig
