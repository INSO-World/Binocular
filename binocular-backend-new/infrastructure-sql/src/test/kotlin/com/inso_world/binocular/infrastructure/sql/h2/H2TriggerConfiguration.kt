package com.inso_world.binocular.infrastructure.sql.h2

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

@Configuration
@Profile("h2")
class H2TriggerConfiguration {

    @Autowired
    private lateinit var dataSource: DataSource

    @EventListener(ApplicationReadyEvent::class)
    fun loadTriggers() {
        val resource = ClassPathResource("db/h2/prevent_cycles_trigger_h2.sql")
        val databasePopulator = ResourceDatabasePopulator(resource)
        databasePopulator.execute(dataSource)
    }
}
