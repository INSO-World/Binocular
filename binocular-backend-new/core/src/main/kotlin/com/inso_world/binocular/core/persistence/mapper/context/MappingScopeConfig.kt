package com.inso_world.binocular.core.persistence.mapper.context

import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal open class MappingScopeConfig {
    @Bean
    open fun mappingScope() = MappingScope()

    @Bean
    open fun customScopeConfigurer(mappingScope: MappingScope) =
        CustomScopeConfigurer().apply {
            addScope("mapping", mappingScope)
        }
}
