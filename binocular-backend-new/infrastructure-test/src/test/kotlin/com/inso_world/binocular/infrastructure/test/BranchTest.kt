package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import com.inso_world.binocular.infrastructure.test.base.BaseInfrastructureSpringTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired

/**
 * Integration tests for Branch persistence via BranchInfrastructurePort.
 *
 * Since #454 a branch references its repository through [com.inso_world.binocular.model.Branch.repositoryId]
 * and its head through [com.inso_world.binocular.model.Branch.headSha]; the object relations
 * `repository`/`head` and the derived `commits` getter are deprecated leftovers.
 */
internal class BranchTest : BaseInfrastructureSpringTest() {
    @Autowired
    lateinit var branchPort: BranchInfrastructurePort

    @Test
    fun `load branch by provider id`() {
        val expected = TestDataProvider.testBranches.first()
        val loaded = branchPort.findByIid(expected.iid)
        assertNotNull(loaded)
        loaded!!
        assertAll(
            { assertEquals(expected.id, loaded.id, "id") },
            { assertEquals(expected.name, loaded.name, "name") },
            { assertEquals(expected.fullName, loaded.fullName, "fullName") },
            { assertEquals(expected.category, loaded.category, "category") },
        )
    }

    @Test
    fun `find files by provider branch id returns entries when linked`() {
        val branch = TestDataProvider.testBranches.first()
        val files = branchPort.findFilesByBranchId(requireNotNull(branch.id))
        // ArangodbInfrastructureDataSetup links branch1->file1,file2
        assert(files.isNotEmpty())
    }

    @Test
    fun `branch head sha survives the round trip`() {
        val expected = TestDataProvider.testBranches.first()
        val loaded = branchPort.findByIid(expected.iid)
        assertNotNull(loaded)
        loaded!!

        assertAll(
            { assertThat(loaded.headSha).isNotBlank() },
            { assertEquals(expected.headSha, loaded.headSha, "headSha") },
        )
    }

    @Test
    fun `branch references its repository via repositoryId`() {
        val expected = TestDataProvider.testBranches.first()
        val loaded = branchPort.findByIid(expected.iid)
        assertNotNull(loaded)
        loaded!!

        assertEquals(expected.repositoryId, loaded.repositoryId, "repositoryId")
    }
}
