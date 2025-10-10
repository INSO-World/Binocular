package com.inso_world.binocular.infrastructure.test

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * Base class for infrastructure integration tests. Profiles and properties are now managed via
 * src/test/resources/application.yaml and per-profile files (e.g., application-arangodb.yaml).
 * This base also ensures DB is populated with TestDataProvider data before each test.
 */
@SpringBootTest(classes = [TestApplication::class])
abstract class BaseInfrastructureSpringTest {
    @Autowired
    protected lateinit var testDataSetupService: TestDataSetupService

    @BeforeEach
    fun baseSetup() {
        testDataSetupService.clearAllData()
        testDataSetupService.setupTestData()
    }
}
