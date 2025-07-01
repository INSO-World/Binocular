package com.inso_world.binocular.web.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

/**
 * Configuration class to re-enable DataSource and JPA auto-configuration when the "sql" profile is active.
 * This is necessary because these auto-configurations are disabled by default in the main application class.
 */
@Configuration
@Profile("sql")
@Import(
    DataSourceAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class
)
class SqlAutoConfig
