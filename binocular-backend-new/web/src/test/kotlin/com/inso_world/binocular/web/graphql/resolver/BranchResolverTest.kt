package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.BaseDbTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the Branch resolver functionality.
 * This class extends BaseDbTest to leverage the test data setup.
 */
class BranchResolverTest : BaseDbTest() {

    @Nested
    inner class BasicFunctionality {
        @Test
        fun `should retrieve branch with all fields`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    branch(id: "1") {
                        id
                        branch
                        active
                        tracksFileRenames
                        latestCommit
                    }
                }
            """)
                .execute()
                .path("branch")
                .entity(JsonNode::class.java)
                .get()

            // Verify branch data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Branch ID mismatch") },
                { assertEquals("main", result.get("branch").asText(), "Branch name mismatch") },
                { assertEquals(true, result.get("active").asBoolean(), "Branch active status mismatch") },
                { assertEquals(true, result.get("tracksFileRenames").asBoolean(), "Branch tracksFileRenames mismatch") },
                { assertEquals("abc123", result.get("latestCommit").asText(), "Branch latestCommit mismatch") }
            )
        }
    }

    @Nested
    inner class RelationshipTests {
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
                { assertEquals("1", result.get("id").asText(), "Branch ID mismatch") },
                { assertEquals("main", result.get("branch").asText(), "Branch name mismatch") },
                { assertEquals(true, result.get("active").asBoolean(), "Branch active status mismatch") },
                { assertEquals(true, result.get("tracksFileRenames").asBoolean(), "Branch tracksFileRenames mismatch") },
                { assertEquals("abc123", result.get("latestCommit").asText(), "Branch latestCommit mismatch") }
            )

            // Verify files
            val files = result.get("files")
            assertNotNull(files, "Files should not be null")
            assertEquals(2, files.size(), "Should have 2 files")

            // Create a list of file paths from the result
            val filePaths = (0 until files.size()).map { files.get(it).get("path").asText() }

            // Verify that both files are present
            assertAll(
                { assertTrue(filePaths.contains("src/main/kotlin/com/example/Main.kt"), "Should contain Main.kt file") },
                { assertTrue(filePaths.contains("src/main/kotlin/com/example/Utils.kt"), "Should contain Utils.kt file") }
            )
        }

        @Test
        fun `should retrieve second branch with related files`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    branch(id: "2") {
                        id
                        branch
                        active
                        tracksFileRenames
                        latestCommit
                        files {
                            id
                            path
                            webUrl
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
                { assertEquals("2", result.get("id").asText(), "Branch ID mismatch") },
                { assertEquals("feature/new-feature", result.get("branch").asText(), "Branch name mismatch") },
                { assertEquals(true, result.get("active").asBoolean(), "Branch active status mismatch") },
                { assertEquals(false, result.get("tracksFileRenames").asBoolean(), "Branch tracksFileRenames mismatch") },
                { assertEquals("def456", result.get("latestCommit").asText(), "Branch latestCommit mismatch") }
            )

            // Verify files
            val files = result.get("files")
            assertNotNull(files, "Files should not be null")
            assertEquals(1, files.size(), "Should have 1 file")

            // Verify the file data
            val file = files.get(0)
            assertAll(
                { assertEquals("2", file.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Utils.kt", file.get("path").asText(), "File path mismatch") }
            )
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle non-existent branch`() {
            // Create a test query for a branch that doesn't exist in the test data
            // This should return an error
            graphQlTester.document("""
                query {
                    branch(id: "999") {
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
                .errors()
                .expect { error ->
                    error.message?.contains("Branch not found with id: 999") ?: false
                }
                .verify()
        }
    }
}
