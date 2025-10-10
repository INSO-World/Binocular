package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.service.MilestoneInfrastructurePort
import com.inso_world.binocular.model.Milestone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MilestoneTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var milestonePort: MilestoneInfrastructurePort

    @BeforeEach
    fun setup() {
        milestonePort.deleteAll()
    }

    @AfterEach
    fun cleanup() {
        milestonePort.deleteAll()
    }

    @Test
    fun `create milestone and load it by id`() {
        val ms = Milestone(title = "1.0 Release", state = "active")
        val created = milestonePort.create(ms)
        val id = requireNotNull(created.id)
        assertNotNull(id)

        val loaded = milestonePort.findById(id)
        assertNotNull(loaded)
        loaded!!
        assertEquals(created.id, loaded.id)
        assertEquals(ms.title, loaded.title)
        assertEquals(ms.state, loaded.state)
    }

    @Test
    fun `findAll returns created milestones`() {
        val m1 = milestonePort.create(Milestone(title = "M1"))
        val m2 = milestonePort.create(Milestone(title = "M2"))
        val ids = milestonePort.findAll().mapNotNull { it.id }.toSet()
        assert(ids.contains(m1.id) && ids.contains(m2.id))
    }
}
