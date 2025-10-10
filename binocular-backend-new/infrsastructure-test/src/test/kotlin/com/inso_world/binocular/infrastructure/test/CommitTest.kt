package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.CommitInfrastructurePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CommitTest : BaseInfrastructureSpringTest() {

    @Autowired
    lateinit var commitPort: CommitInfrastructurePort

    @Test
    fun `load commit by provider id`() {
        val expected = TestDataProvider.testCommits.first()
        val loaded = commitPort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.sha, loaded.sha)
        assertEquals(expected.message, loaded.message)
    }

    @Test
    fun `commit relations resolved via ports`() {
        val commit1 = TestDataProvider.testCommits[0]
        val commit2 = TestDataProvider.testCommits[1]
        val commit1Id = requireNotNull(commit1.id)
        val commit2Id = requireNotNull(commit2.id)

        // builds
        val builds = commitPort.findBuildsByCommitId(commit1Id)
        assertTrue(builds.any { it.id == TestDataProvider.testBuilds[0].id })

        // parent/child
        val parents = commitPort.findParentCommitsByChildCommitId(commit2Id)
        assertTrue(parents.any { it.id == commit1.id })
        val children = commitPort.findChildCommitsByParentCommitId(commit1Id)
        assertTrue(children.any { it.id == commit2.id })

        // files
        val files = commitPort.findFilesByCommitId(commit1Id)
        assertTrue(files.any { it.id == TestDataProvider.testFiles[0].id })

        // modules
        val modules = commitPort.findModulesByCommitId(commit1Id)
        assertTrue(modules.any { it.id == TestDataProvider.testModules[0].id })

        // users
        val users = commitPort.findUsersByCommitId(commit1Id)
        assertTrue(users.any { it.id == TestDataProvider.testUsers[0].id })

        // issues
        val issues = commitPort.findIssuesByCommitId(commit1Id)
        assertTrue(issues.any { it.id == TestDataProvider.testIssues[0].id })
    }
}
