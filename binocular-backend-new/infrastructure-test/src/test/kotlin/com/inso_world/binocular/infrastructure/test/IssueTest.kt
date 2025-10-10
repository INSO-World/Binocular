package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.IssueInfrastructurePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class IssueTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var issuePort: IssueInfrastructurePort

    @Test
    fun `load issue by provider id`() {
        val expected = TestDataProvider.testIssues.first()
        val loaded = issuePort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.title, loaded.title)
        assertEquals(expected.state, loaded.state)
    }

    @Test
    fun `issue relations resolved via ports`() {
        val issue1 = TestDataProvider.testIssues[0]
        val issue1Id = requireNotNull(issue1.id)

        val accs = issuePort.findAccountsByIssueId(issue1Id)
        val commits = issuePort.findCommitsByIssueId(issue1Id)
        val milestones = issuePort.findMilestonesByIssueId(issue1Id)
        val notes = issuePort.findNotesByIssueId(issue1Id)
        val users = issuePort.findUsersByIssueId(issue1Id)

        assertTrue(accs.any { it.id == TestDataProvider.testAccounts[0].id })
        assertTrue(commits.any { it.id == TestDataProvider.testCommits[0].id })
        assertTrue(milestones.any { it.id == TestDataProvider.testMilestones[0].id })
        assertTrue(notes.any { it.id == TestDataProvider.testNotes[0].id })
        assertTrue(users.any { it.id == TestDataProvider.testUsers[0].id })
    }
}
