package com.inso_world.binocular.web.base

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.web.BinocularWebApplication
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
    classes = [BinocularWebApplication::class],
    // TODO ????
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
    ]
)
@AutoConfigureGraphQlTester
@ContextConfiguration(
    initializers = [
        com.inso_world.binocular.infrastructure.arangodb.ArangodbTestConfig.Initializer::class,
    ]
)
internal abstract class AbstractWebIntegrationTest : BaseIntegrationTest()
