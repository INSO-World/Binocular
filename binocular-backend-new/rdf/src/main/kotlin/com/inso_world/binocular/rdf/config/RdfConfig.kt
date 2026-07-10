package com.inso_world.binocular.rdf.config

import com.arangodb.ArangoDB
import com.arangodb.ArangoDatabase
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "binocular.rdf")
class RdfConfig {
    lateinit var arangodb: ArangoDBConfig
    lateinit var output: OutputConfig

    @Bean
    fun arangoDatabase(): ArangoDatabase {
        val arango =
            ArangoDB
                .Builder()
                .host(arangodb.host, arangodb.port)
                .user(arangodb.user)
                .password(arangodb.password)
                .build()
        return arango.db(arangodb.database)
    }
}

class ArangoDBConfig {
    var host: String = "localhost"
    var port: Int = 8529
    var user: String = "root"
    var password: String = ""
    var database: String = "binocular"
}

class OutputConfig {
    var path: String = "./output/knowledge-graph.ttl"
}
