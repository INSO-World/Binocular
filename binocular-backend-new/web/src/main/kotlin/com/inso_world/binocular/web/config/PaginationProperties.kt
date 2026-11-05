package com.inso_world.binocular.web.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuration properties for pagination.
 * These properties can be overridden in application.properties or application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "pagination")
class PaginationProperties {
    /**
     * Default page number (1-based) when not specified.
     */
    var defaultPage = 1

    /**
     * Default number of items per page when not specified.
     */
    var defaultSize = 20

    /**
     * Maximum allowed number of items per page.
     */
    var maxSize = 100
}
