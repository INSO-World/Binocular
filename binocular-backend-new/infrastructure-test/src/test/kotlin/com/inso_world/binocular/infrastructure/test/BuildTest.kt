package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BuildInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class BuildTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var buildPort: BuildInfrastructurePort

    @Test
    fun `load build by provider id`() {
        val expected = TestDataProvider.testBuilds.first()
        val loaded = buildPort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.status, loaded.status)
        assertEquals(expected.ref, loaded.ref)
    }

    @Test
    fun `findAll returns provider builds`() {
        val ids = buildPort.findAll().mapNotNull { it.id }.toSet()
        val expectedIds = TestDataProvider.testBuilds.mapNotNull { it.id }.toSet()
        assert(ids.containsAll(expectedIds))
    }
}
