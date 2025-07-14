package com.inso_world.binocular.infrastructure.sql.integration.service.base

import com.inso_world.binocular.core.integration.base.BaseIntegrationTest
import com.inso_world.binocular.infrastructure.sql.TestApplication
import com.inso_world.binocular.infrastructure.sql.integration.SqlInfrastructureTestDataSetupService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ExtendWith(SpringExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
internal open class BaseServiceTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var testDataSetupService: SqlInfrastructureTestDataSetupService
//    protected lateinit var project: Project
//    protected lateinit var repository: Repository

    @BeforeEach
    internal fun setUp() {
        testDataSetupService.setupTestData()
//        project =
//            Project(
//                name = "test project",
//            )
//
//        repository =
//            Repository(
//                name = "test repository",
//            )
//        project.repo = repository
    }

    @AfterEach
    internal fun tearDown() {
        testDataSetupService.clearAllData()
    }
}
