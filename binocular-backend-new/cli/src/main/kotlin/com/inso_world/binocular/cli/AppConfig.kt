package com.inso_world.binocular.cli

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

//@Profile("arangodb")
//@Configuration
//@Import(value = [AdbConfig::class])
//class BinocularCliConfiguration : BinocularAppConfig() {
////  @get:Profile("arangodb")
////  @set:Profile("arangodb")
//  @Autowired
//  private lateinit var config: AdbConfig
//}

//@Profile("!arangodb")
@Configuration
@ConfigurationProperties(prefix = "binocular")
class BinocularCliConfiguration {
  lateinit var index: IndexConfig
}

class IndexConfig(
  val path: String,
)
