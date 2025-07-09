package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.graphql.base.GraphQlControllerTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the File resolver functionality.
 * This class extends GraphQlControllerTest to leverage the test data setup.
 */
internal class FileResolverTest : GraphQlControllerTest() {
    @Nested
    inner class BasicFunctionality {
        @Test
        fun `should retrieve file with all fields`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    file(id: "1") {
                        id
                        path
                        webUrl
                        maxLength
                    }
                }
            """,
                    ).execute()
                    .path("file")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify file data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Main.kt", result.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Main.kt", result.get("webUrl").asText(), "File webUrl mismatch") },
                { assertEquals(1000, result.get("maxLength").asInt(), "File maxLength mismatch") },
            )
        }
    }

    @Nested
    inner class RelationshipTests {
        @Test
        fun `should retrieve file with related branches`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    file(id: "1") {
                        id
                        path
                        webUrl
                        branches {
                            id
                            branch
                            active
                            tracksFileRenames
                        }
                    }
                }
            """,
                    ).execute()
                    .path("file")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify file data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Main.kt", result.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Main.kt", result.get("webUrl").asText(), "File webUrl mismatch") },
            )

            // Verify branches
            val branches = result.get("branches")
            assertNotNull(branches, "Branches should not be null")
            assertEquals(1, branches.size(), "Should have 1 branch")

            val branch = branches.get(0)
            assertAll(
                { assertEquals("1", branch.get("id").asText(), "Branch ID mismatch") },
                { assertEquals("main", branch.get("branch").asText(), "Branch name mismatch") },
                { assertEquals(true, branch.get("active").asBoolean(), "Branch active status mismatch") },
                { assertEquals(true, branch.get("tracksFileRenames").asBoolean(), "Branch tracksFileRenames mismatch") },
            )
        }

        @Test
        fun `should retrieve second file with related branches`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
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
            """,
                    ).execute()
                    .path("file")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify file data
            assertAll(
                { assertEquals("2", result.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Utils.kt", result.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Utils.kt", result.get("webUrl").asText(), "File webUrl mismatch") },
            )

            // Verify branches
            val branches = result.get("branches")
            assertNotNull(branches, "Branches should not be null")
            assertEquals(2, branches.size(), "Should have 2 branches")

            // Create a list of branch IDs from the result
            val branchIds = (0 until branches.size()).map { branches.get(it).get("id").asText() }

            // Verify that both branch IDs are present
            assertAll(
                { assertTrue(branchIds.contains("1"), "Should contain branch ID 1") },
                { assertTrue(branchIds.contains("2"), "Should contain branch ID 2") },
            )
        }

        @Test
        fun `should retrieve file with related commits`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    file(id: "1") {
                        id
                        path
                        webUrl
                        commits {
                            id
                            sha
                            message
                        }
                    }
                }
            """,
                    ).execute()
                    .path("file")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify file data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Main.kt", result.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Main.kt", result.get("webUrl").asText(), "File webUrl mismatch") },
            )

            // Verify commits
            val commits = result.get("commits")
            assertNotNull(commits, "Commits should not be null")
            assertEquals(1, commits.size(), "Should have 1 commit")

            val commit = commits.get(0)
            assertAll(
                { assertEquals("1", commit.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", commit.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", commit.get("message").asText(), "Commit message mismatch") },
            )
        }

        @Test
        fun `should retrieve file with related modules`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    file(id: "1") {
                        id
                        path
                        webUrl
                        modules {
                            id
                            path
                        }
                    }
                }
            """,
                    ).execute()
                    .path("file")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify file data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Main.kt", result.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Main.kt", result.get("webUrl").asText(), "File webUrl mismatch") },
            )

            // Verify modules
            val modules = result.get("modules")
            assertNotNull(modules, "Modules should not be null")
            assertEquals(1, modules.size(), "Should have 1 module")

            val module = modules.get(0)
            assertAll(
                { assertEquals("1", module.get("id").asText(), "Module ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/core", module.get("path").asText(), "Module path mismatch") },
            )
        }

        @Test
        fun `should retrieve file with related users`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    file(id: "1") {
                        id
                        path
                        webUrl
                        users {
                            id
                            gitSignature
                        }
                    }
                }
            """,
                    ).execute()
                    .path("file")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify file data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Main.kt", result.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Main.kt", result.get("webUrl").asText(), "File webUrl mismatch") },
            )

            // Verify users
            val users = result.get("users")
            assertNotNull(users, "Users should not be null")
            if (users.size() > 0) {
                val user = users.get(0)
                assertAll(
                    { assertEquals("1", user.get("id").asText(), "User ID mismatch") },
                    { assertEquals("John Doe <john.doe@example.com>", user.get("gitSignature").asText(), "User gitSignature mismatch") },
                )
            }
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle non-existent file`() {
            // Create a test query for a file that doesn't exist in the test data
            // This should return an error
            graphQlTester
                .document(
                    """
                query {
                    file(id: "999") {
                        id
                        path
                        webUrl
                    }
                }
            """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("File not found with id: 999") ?: false
                }.verify()
        }
    }
}
