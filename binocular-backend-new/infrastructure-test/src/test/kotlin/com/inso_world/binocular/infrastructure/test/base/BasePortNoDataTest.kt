package com.inso_world.binocular.infrastructure.test.base

import org.junit.jupiter.api.BeforeEach

// @EnableAutoConfiguration
// @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
// @ExtendWith(SpringExtension::class)
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
// //        entityManager.clear()
// //        projectRepository.deleteAll()
// //        repositoryRepository.deleteAll()
// //        commitRepository.deleteAll()
// //        testDataSetupService.clearAllData()
// //        userRepository.deleteAll()
//    }
}
