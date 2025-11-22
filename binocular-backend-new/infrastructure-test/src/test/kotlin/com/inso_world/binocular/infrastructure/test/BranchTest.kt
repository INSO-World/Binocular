package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for Branch persistence via BranchInfrastructurePort.
 * Tests verify that domain model semantics (particularly the derived commits getter
 * and head property validation) are preserved through the infrastructure layer.
 */
internal class BranchTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var branchPort: BranchInfrastructurePort

    @Test
    fun `load branch by provider id`() {
        val expected = TestDataProvider.testBranches.first()
        val loaded = branchPort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!
        assertEquals(expected.id, loaded.id)
        assertEquals(expected.name, loaded.name)
        assertEquals(expected.active, loaded.active)
        assertEquals(expected.tracksFileRenames, loaded.tracksFileRenames)
        assertEquals(expected.latestCommit, loaded.latestCommit)
    }

    @Test
    fun `find files by provider branch id returns entries when linked`() {
        val branch = TestDataProvider.testBranches.first()
        val files = branchPort.findFilesByBranchId(requireNotNull(branch.id))
        // ArangodbInfrastructureDataSetup links branch1->file1,file2
        assert(files.isNotEmpty())
    }

    @Test
    fun `branch commits getter returns all reachable commits in topo order`() {
        val branch = TestDataProvider.testBranches.first()
        val commits = branch.commits

        // Commits should be non-empty (at least contains head)
        assert(commits.isNotEmpty())
        assert(commits.contains(branch.head))

        // First commit should be the head (children-before-parents order)
        assertEquals(branch.head, commits.first())
    }

    @Test
    fun `branch head must be from same repository`() {
        val expected = TestDataProvider.testBranches.first()
        val loaded = branchPort.findById(requireNotNull(expected.id))
        assertNotNull(loaded)
        loaded!!

        // Verify head is from same repository
        assertEquals(loaded.repository.id, loaded.head.repository?.id)
    }

    @Test
    fun `branch auto-registers with repository during construction`() {
        val branch = TestDataProvider.testBranches.first()
        val repository = branch.repository

        // Branch should be in repository's branches collection
        assert(repository.branches.contains(branch))
    }
}
