package com.inso_world.binocular.rdf

import com.inso_world.binocular.rdf.service.KnowledgeGraphBuilder
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.slf4j.LoggerFactory

@SpringBootApplication
class RdfApplication(private val knowledgeGraphBuilder: KnowledgeGraphBuilder) {

    private val logger = LoggerFactory.getLogger(RdfApplication::class.java)

    @Bean
    fun run() = ApplicationRunner {
        logger.info("RDF Knowledge Graph Builder started")
        knowledgeGraphBuilder.buildAndExport()
        logger.info("Knowledge graph export completed")
    }
}

fun main(args: Array<String>) {
    runApplication<RdfApplication>(*args)
}
