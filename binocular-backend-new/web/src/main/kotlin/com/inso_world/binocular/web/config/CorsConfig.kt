package com.inso_world.binocular.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig {

  @Bean
  fun corsConfigurer(): WebMvcConfigurer {
    return object : WebMvcConfigurer {
      override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // Allow all paths
          .allowedOrigins("*")     // Allow any origin
          .allowedMethods("*")     // Allow all HTTP methods
          .allowedHeaders("*")     // Allow all headers
      }
    }
  }
}
