package com.inso_world.binocular.infrastructure.sql.mapper.context

import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class MappingScopeConfig {
    @Bean fun mappingScope() = MappingScope()

    @Bean
    fun customScopeConfigurer(mappingScope: MappingScope) =
        CustomScopeConfigurer().apply {
            addScope("mapping", mappingScope)
        }
}
