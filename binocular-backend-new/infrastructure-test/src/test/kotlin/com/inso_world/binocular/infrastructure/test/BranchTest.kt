package com.inso_world.binocular.infrastructure.test

import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.core.service.BranchInfrastructurePort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * create and load branch using BranchInfrastructurePort from core module package
 */
class BranchTest : BaseInfrastructureSpringTest() {

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
}
