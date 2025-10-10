package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.FileInfrastructurePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FileTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var filePort: FileInfrastructurePort

    @Test
    fun `load file by provider id`() {
        val expected = TestDataProvider.testFiles.first()
        val loaded = filePort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.path, loaded.path)
        assertEquals(expected.webUrl, loaded.webUrl)
    }

    @Test
    fun `findAll returns provider files`() {
        val ids = filePort.findAll().mapNotNull { it.id }.toSet()
        val expectedIds = TestDataProvider.testFiles.mapNotNull { it.id }.toSet()
        assert(ids.containsAll(expectedIds))
    }
}
