package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.MergeRequestInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class MergeRequestTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var mergeRequestPort: MergeRequestInfrastructurePort

    @Test
    fun `load merge request by provider id`() {
        val expected = TestDataProvider.testMergeRequests.first()
        val loaded = mergeRequestPort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.title, loaded.title)
        assertEquals(expected.state, loaded.state)
    }

    @Test
    fun `merge request relations resolved via ports`() {
        val mr1 = TestDataProvider.testMergeRequests[0]
        val mr1Id = requireNotNull(mr1.id)

        val accs = mergeRequestPort.findAccountsByMergeRequestId(mr1Id)
        val milestones = mergeRequestPort.findMilestonesByMergeRequestId(mr1Id)
        val notes = mergeRequestPort.findNotesByMergeRequestId(mr1Id)

        assertTrue(accs.any { it.id == TestDataProvider.testAccounts[0].id })
        assertTrue(milestones.any { it.id == TestDataProvider.testMilestones[0].id })
        assertTrue(notes.any { it.id == TestDataProvider.testNotes[0].id })
    }
}
