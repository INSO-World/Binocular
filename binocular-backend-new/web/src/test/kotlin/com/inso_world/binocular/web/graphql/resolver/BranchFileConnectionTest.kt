package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.BaseDbTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the BranchFileConnection edge relationship.
 * This class extends BaseDbTest to leverage the test data setup.
 */
class BranchFileConnectionTest : BaseDbTest() {

    @Nested
    inner class BranchToFileRelationship {
        @Test
        fun `should retrieve branch with related files`() {
            val result: JsonNode = graphQlTester.document("""
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
                .entity(JsonNode::class.java)
                .get()

            // Verify branch data
            assertAll(
                { assertEquals(testBranches[0].id, result.get("id").asText(), "Branch ID mismatch") },
                { assertEquals(testBranches[0].branch, result.get("branch").asText(), "Branch name mismatch") },
                { assertEquals(testBranches[0].active, result.get("active").asBoolean(), "Branch active status mismatch") },
                { assertEquals(testBranches[0].tracksFileRenames, result.get("tracksFileRenames").asBoolean(), "Branch tracksFileRenames mismatch") },
                { assertEquals(testBranches[0].latestCommit, result.get("latestCommit").asText(), "Branch latestCommit mismatch") }
            )

            // Verify files
            val files = result.get("files")
            assertNotNull(files, "Files should not be null")
            assertEquals(2, files.size(), "Should have 2 files")

            // Create a list of file IDs from the result
            val fileIds = (0 until files.size()).map { files.get(it).get("id").asText() }

            // Verify that the file IDs match the expected file IDs
            assertAll(
                { assertTrue(fileIds.contains(testFiles[0].id), "Should contain first test file ID") },
                { assertTrue(fileIds.contains(testFiles[1].id), "Should contain second test file ID") }
            )
        }

        @Test
        fun `should retrieve second branch with only one file`() {
            val result: JsonNode = graphQlTester.document("""
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
                .entity(JsonNode::class.java)
                .get()

            // Verify branch data
            assertAll(
                { assertEquals(testBranches[1].id, result.get("id").asText(), "Branch ID mismatch") },
                { assertEquals(testBranches[1].branch, result.get("branch").asText(), "Branch name mismatch") }
            )

            // Verify files
            val files = result.get("files")
            assertNotNull(files, "Files should not be null")
            assertEquals(1, files.size(), "Should have 1 file")

            // Verify the file data
            val file = files.get(0)
            assertAll(
                { assertEquals(testFiles[1].id, file.get("id").asText(), "File ID mismatch") },
                { assertEquals(testFiles[1].path, file.get("path").asText(), "File path mismatch") }
            )
        }
    }

    @Nested
    inner class FileToBranchRelationship {
        @Test
        fun `should retrieve file with related branches`() {
            val result: JsonNode = graphQlTester.document("""
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
                .entity(JsonNode::class.java)
                .get()

            // Verify file data
            assertAll(
                { assertEquals(testFiles[0].id, result.get("id").asText(), "File ID mismatch") },
                { assertEquals(testFiles[0].path, result.get("path").asText(), "File path mismatch") },
                { assertEquals(testFiles[0].webUrl, result.get("webUrl").asText(), "File webUrl mismatch") }
            )

            // Verify branches
            val branches = result.get("branches")
            assertNotNull(branches, "Branches should not be null")
            assertEquals(1, branches.size(), "Should have 1 branch")

            // Verify the branch data
            val branch = branches.get(0)
            assertAll(
                { assertEquals(testBranches[0].id, branch.get("id").asText(), "Branch ID mismatch") },
                { assertEquals(testBranches[0].branch, branch.get("branch").asText(), "Branch name mismatch") }
            )
        }

        @Test
        fun `should retrieve second file with related branches`() {
            val result: JsonNode = graphQlTester.document("""
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
                .entity(JsonNode::class.java)
                .get()

            // Verify file data
            assertAll(
                { assertEquals(testFiles[1].id, result.get("id").asText(), "File ID mismatch") },
                { assertEquals(testFiles[1].path, result.get("path").asText(), "File path mismatch") },
                { assertEquals(testFiles[1].webUrl, result.get("webUrl").asText(), "File webUrl mismatch") }
            )

            // Verify branches
            val branches = result.get("branches")
            assertNotNull(branches, "Branches should not be null")
            assertEquals(2, branches.size(), "Should have 2 branches")

            // Create a list of branch IDs from the result
            val branchIds = (0 until branches.size()).map { branches.get(it).get("id").asText() }

            // Verify that the branch IDs match the expected branch IDs
            assertAll(
                { assertTrue(branchIds.contains(testBranches[0].id), "Should contain first test branch ID") },
                { assertTrue(branchIds.contains(testBranches[1].id), "Should contain second test branch ID") }
            )
        }
    }
}
