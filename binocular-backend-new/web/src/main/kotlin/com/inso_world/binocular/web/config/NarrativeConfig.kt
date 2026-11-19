package com.inso_world.binocular.web.config

import com.inso_world.binocular.core.service.narrative.NarrativeInfrastructurePort
import com.inso_world.binocular.core.service.narrative.NarrativeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Makes the narrative composition available to the web application.
 *
 * ## Purpose
 * [NarrativeService] carries no Spring annotation on purpose: its port is only implemented by the
 * ArangoDB adapter, and a component-scanned service in `core` would break the `cli` application, which
 * scans the same package but may run without that adapter. The web application always runs with the
 * ArangoDB adapter, so it declares the service as a bean here.
 *
 * ## Usage
 * ```kotlin
 * @Controller
 * internal class NarrativeController(private val narrativeService: NarrativeService)
 * ```
 */
@Configuration
class NarrativeConfig {
    /**
     * Creates the narrative service on top of the adapter that implements the narrative port.
     *
     * @param narrativePort Source of the raw commit, issue and build activity.
     * @return The service that composes narratives.
     */
    @Bean
    fun narrativeService(narrativePort: NarrativeInfrastructurePort): NarrativeService = NarrativeService(narrativePort)
}
