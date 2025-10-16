package com.inso_world.binocular.infrastructure.sql.integration.persistence.mapper.base

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.sql.mapper.context.MappingScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootApplication(
    scanBasePackages = ["com.inso_world.binocular.infrastructure.sql.mapper", "com.inso_world.binocular.core"],
    exclude = [
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration::class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration::class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration::class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration::class,
        org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration::class
    ]
)
private class MapperTestApplication

@SpringBootTest(classes = [MapperTestApplication::class])
internal class BaseMapperTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var mappingScope: MappingScope

    @BeforeEach
    fun openSession() {
        mappingScope.startSession()
    }

    @AfterEach
    fun closeSession() {
        mappingScope.endSession()
    }
}
