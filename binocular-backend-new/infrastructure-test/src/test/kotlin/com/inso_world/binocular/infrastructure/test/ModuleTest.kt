package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.ModuleInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class ModuleTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var modulePort: ModuleInfrastructurePort

    @Test
    fun `module relations resolved via ports`() {
        val module1 = TestDataProvider.testModules[0]
        val module2 = TestDataProvider.testModules[1]
        val module1Id = requireNotNull(module1.id)
        val module2Id = requireNotNull(module2.id)

        val files = modulePort.findFilesByModuleId(module1Id)
        assertTrue(files.any { it.id == TestDataProvider.testFiles[0].id })

        val children = modulePort.findChildModulesByModuleId(module1Id)
        assertTrue(children.any { it.id == module2.id })

        val parents = modulePort.findParentModulesByModuleId(module2Id)
        assertTrue(parents.any { it.id == module1.id })
    }
}
