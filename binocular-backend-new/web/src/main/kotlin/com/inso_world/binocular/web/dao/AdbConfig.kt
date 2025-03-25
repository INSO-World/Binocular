package com.inso_world.binocular.web.dao

import com.arangodb.ArangoDB
import com.arangodb.springframework.annotation.EnableArangoRepositories
import com.arangodb.springframework.config.ArangoConfiguration
import org.springframework.context.annotation.Configuration

@Configuration
@EnableArangoRepositories(basePackages = ["com.inso_world.binocular.web.dao", "com.inso_world.binocular.web.entity"])
class AdbConfig : ArangoConfiguration {
  override fun arango(): ArangoDB.Builder {
    return ArangoDB.Builder()
      .host("localhost", 8529)
//      .user("admin")
  }

  override fun database(): String {
    return "binocular-repo"
  }

  override fun returnOriginalEntities(): Boolean {
    return false
  }
}
