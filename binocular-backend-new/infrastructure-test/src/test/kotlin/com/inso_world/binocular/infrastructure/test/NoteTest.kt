package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.NoteInfrastructurePort
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class NoteTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var notePort: NoteInfrastructurePort

    @Test
    fun `note relations resolved via ports`() {
        val note1 = TestDataProvider.testNotes[0]
        val note1Id = requireNotNull(note1.id)

        val accs = notePort.findAccountsByNoteId(note1Id)
        val issues = notePort.findIssuesByNoteId(note1Id)
        val mrs = notePort.findMergeRequestsByNoteId(note1Id)

        assertTrue(accs.any { it.id == TestDataProvider.testAccounts[0].id })
        assertTrue(issues.any { it.id == TestDataProvider.testIssues[0].id })
        assertTrue(mrs.any { it.id == TestDataProvider.testMergeRequests[0].id })
    }
}
