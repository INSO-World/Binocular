package com.inso_world.binocular.web.persistence.dao.nosql.arangodb

import com.arangodb.ArangoDB
import com.arangodb.springframework.annotation.EnableArangoRepositories
import com.arangodb.springframework.config.ArangoConfiguration
import com.inso_world.binocular.web.BinocularAppConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("arangodb")
@Configuration
@EnableArangoRepositories(basePackages = ["com.inso_world.binocular.web.persistence", "com.inso_world.binocular.web.entity"])
class AdbConfig(
  @Autowired private val appConfig: BinocularAppConfig,
) : ArangoConfiguration {
  override fun arango(): ArangoDB.Builder {
    var builder = ArangoDB.Builder()
      .host(appConfig.database.host, appConfig.database.port.toInt())

    builder = appConfig.database.user?.let { builder.user(it) }
    builder = appConfig.database.password?.let { builder.password(it) }

    return builder
  }

  override fun database(): String {
    return appConfig.database.databaseName
  }

  override fun returnOriginalEntities(): Boolean {
    return false
  }
}
