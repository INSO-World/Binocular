package com.inso_world.binocular.web.graphql.relationship

import com.inso_world.binocular.web.BaseDbTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the BranchFileConnection edge relationship.
 * This class extends BaseDbTest to leverage the test data setup.
 */
class BranchFileConnectionTest : BaseDbTest() {

    data class TestBranch(
        val id: String?,
        val branch: String?,
        val active: Boolean? = null,
        val tracksFileRenames: Boolean? = null,
        val latestCommit: String? = null,
        val files: List<TestFile>? = null
    )

    data class TestFile(
        val id: String?,
        val path: String?,
        val webUrl: String?,
        val maxLength: Int? = null,
        val branches: List<TestBranch>? = null
    )

    @Test
    fun `should retrieve branch with related files`() {
        val result = graphQlTester.document("""
            query {
                branch(id: "1") {
                    id
                    branch
                    active
                    tracksFileRenames
                    latestCommit
                    files {
                        id
                        path
                        webUrl
                        maxLength
                    }
                }
            }
        """)
            .execute()
            .path("branch")
            .entity(TestBranch::class.java)
            .get()

        assertNotNull(result.files)
        assertEquals(2, result.files?.size)

        val fileIds = result.files?.map { it.id }
        fileIds?.let {
            assertTrue(it.contains(testFiles[0].id))
            assertTrue(it.contains(testFiles[1].id))
        }
    }

    @Test
    fun `should retrieve second branch with only one file`() {
        val result = graphQlTester.document("""
            query {
                branch(id: "2") {
                    id
                    branch
                    files {
                        id
                        path
                    }
                }
            }
        """)
            .execute()
            .path("branch")
            .entity(TestBranch::class.java)
            .get()

        assertNotNull(result.files)
        assertEquals(1, result.files?.size)

        val file = result.files?.first()
        assertEquals(testFiles[1].id, file?.id)
        assertEquals(testFiles[1].path, file?.path)
    }

    @Test
    fun `should retrieve file with related branches`() {
        val result = graphQlTester.document("""
            query {
                file(id: "1") {
                    id
                    path
                    webUrl
                    branches {
                        id
                        branch
                    }
                }
            }
        """)
            .execute()
            .path("file")
            .entity(TestFile::class.java)
            .get()

        assertNotNull(result.branches)
        assertEquals(1, result.branches?.size)

        val branch = result.branches?.first()
        assertEquals(testBranches[0].id, branch?.id)
        assertEquals(testBranches[0].branch, branch?.branch)
    }

    @Test
    fun `should retrieve second file with related branches`() {
        val result = graphQlTester.document("""
            query {
                file(id: "2") {
                    id
                    path
                    webUrl
                    branches {
                        id
                        branch
                    }
                }
            }
        """)
            .execute()
            .path("file")
            .entity(TestFile::class.java)
            .get()

        assertNotNull(result.branches)
        assertEquals(2, result.branches?.size)

        val branchIds = result.branches?.map { it.id }
        branchIds?.let {
            assertTrue(it.contains(testBranches[0].id))
            assertTrue(it.contains(testBranches[1].id))
        }
    }
}
