package com.inso_world.binocular.infrastructure.test.base

import com.inso_world.binocular.core.integration.base.InfrastructureDataSetup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired

//@EnableAutoConfiguration
//@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
//@ExtendWith(SpringExtension::class)
@Disabled("Refactored in domain model #454")
internal class BasePortNoDataTest : BaseInfrastructureSpringTest() {
//    @all:Autowired
//    private lateinit var testDataSetupService: InfrastructureDataSetup
//
    @BeforeEach
    fun setupBase() {
        super.baseTearDown()
    }
//
//    @AfterEach
//    fun cleanup() {
//        testDataSetupService.teardown()
////        entityManager.clear()
////        projectRepository.deleteAll()
////        repositoryRepository.deleteAll()
////        commitRepository.deleteAll()
////        testDataSetupService.clearAllData()
////        userRepository.deleteAll()
//    }
}
