package com.inso_world.binocular.cli.performance.commands.base

import com.inso_world.binocular.cli.BinocularCommandLineApplication
import com.inso_world.binocular.cli.integration.TestDataSetupService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [BinocularCommandLineApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
class PerformanceTest {

    @Autowired
    private lateinit var testDataSetupService: TestDataSetupService

    @BeforeEach
    fun setup() {
        testDataSetupService.clearAllData()
    }

    @AfterEach
    fun cleanup() {
//        testDataSetupService.clearAllData()
//        projectRepository.deleteAll()
//        repositoryRepository.deleteAll()
//        branchRepository.deleteAll()
//        commitRepository.deleteAll()
//        userRepository.deleteAll()
    }
}
