package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.AccountInfrastructurePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AccountTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var accountPort: AccountInfrastructurePort

    @Test
    fun `load account by provider id`() {
        val expected = TestDataProvider.testAccounts.first()
        val loaded = accountPort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.platform, loaded.platform)
        assertEquals(expected.login, loaded.login)
        assertEquals(expected.name, loaded.name)
    }

    @Test
    fun `findAll returns provider accounts`() {
        val ids = accountPort.findAll().mapNotNull { it.id }.toSet()
        val expectedIds = TestDataProvider.testAccounts.mapNotNull { it.id }.toSet()
        assert(ids.containsAll(expectedIds))
    }
}
