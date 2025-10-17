package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.graphql.base.GraphQlControllerTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the Commit resolver functionality.
 * This class extends GraphQlControllerTest to leverage the test data setup.
 */
internal class CommitResolverTest : GraphQlControllerTest() {
    @Nested
    inner class BasicFunctionality {
        @Test
        fun `should retrieve commit with all fields`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        webUrl
                        branch
                        stats {
                            additions
                            deletions
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
                { assertEquals("https://example.com/commit/abc123", result.get("webUrl").asText(), "Commit webUrl mismatch") },
                { assertEquals("main", result.get("branch").asText(), "Commit branch mismatch") },
                { assertEquals(10, result.get("stats").get("additions").asLong(), "Commit stats additions mismatch") },
                { assertEquals(5, result.get("stats").get("deletions").asLong(), "Commit stats deletions mismatch") },
            )
        }
    }

    @Nested
    inner class RelationshipTests {
        @Test
        fun `should retrieve commit with related builds`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        builds {
                            id
                            sha
                            status
                            ref
                            tag
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify builds
            val builds = result.get("builds")
            assertNotNull(builds, "Builds should not be null")
            assertEquals(1, builds.size(), "Should have 1 build")

            val build = builds.get(0)
            assertAll(
                { assertEquals("1", build.get("id").asText(), "Build ID mismatch") },
                { assertEquals("abc123", build.get("sha").asText(), "Build SHA mismatch") },
                { assertEquals("success", build.get("status").asText(), "Build status mismatch") },
                { assertEquals("main", build.get("ref").asText(), "Build ref mismatch") },
                { assertEquals("v0.0.1-rc", build.get("tag").asText(), "Build tag mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with related files`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        files {
                            id
                            path
                            webUrl
                            maxLength
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify files
            val files = result.get("files")
            assertNotNull(files, "Files should not be null")
            assertEquals(1, files.size(), "Should have 1 file")

            val file = files.get(0)
            assertAll(
                { assertEquals("1", file.get("id").asText(), "File ID mismatch") },
                { assertEquals("src/main/kotlin/com/example/Main.kt", file.get("path").asText(), "File path mismatch") },
                { assertEquals("https://example.com/files/Main.kt", file.get("webUrl").asText(), "File webUrl mismatch") },
                { assertEquals(Int.MIN_VALUE, file.get("maxLength").asInt(), "File maxLength mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with related modules`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        modules {
                            id
                            path
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
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
        fun `should retrieve commit with related users`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        users {
                            id
                            gitSignature
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify users
            val users = result.get("users")
            assertNotNull(users, "Users should not be null")
            assertEquals(1, users.size(), "Should have 1 user")

            val user = users.get(0)
            assertAll(
                { assertEquals("1", user.get("id").asText(), "User ID mismatch") },
                { assertEquals("John Doe <john.doe@example.com>", user.get("gitSignature").asText(), "User gitSignature mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with related issues`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        issues {
                            id
                            iid
                            title
                            description
                            state
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify issues
            val issues = result.get("issues")
            assertNotNull(issues, "Issues should not be null")
            assertEquals(1, issues.size(), "Should have 1 issue")

            val issue = issues.get(0)
            assertAll(
                { assertEquals("1", issue.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, issue.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", issue.get("title").asText(), "Issue title mismatch") },
                { assertEquals("Users are unable to log in...", issue.get("description").asText(), "Issue description mismatch") },
                { assertEquals("open", issue.get("state").asText(), "Issue state mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with parent relationships`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "2") {
                        id
                        sha
                        message
                        parents
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("2", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("def456", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("Second commit", result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify parents
            val parents = result.get("parents")
            assertNotNull(parents, "Parents should not be null")
            assertEquals(1, parents.size(), "Should have 1 parent")

            val parentString = parents.get(0).asText()
            assertTrue(parentString.contains("id: 1"), "Parent string should contain id: 1")
            assertTrue(parentString.contains("sha: abc123"), "Parent string should contain sha: abc123")
            assertTrue(parentString.contains("shortSha: abc123"), "Parent string should contain shortSha: abc123")
        }

        @Test
        fun `should retrieve commit with child relationships`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(id: "1") {
                        id
                        sha
                        message
                        children {
                            id
                            sha
                            message
                        }
                    }
                }
            """,
                    ).execute()
                    .path("commit")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify commit data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify children
            val children = result.get("children")
            assertNotNull(children, "Children should not be null")
            assertEquals(1, children.size(), "Should have 1 child")

            val child = children.get(0)
            assertAll(
                { assertEquals("2", child.get("id").asText(), "Child ID mismatch") },
                { assertEquals("def456", child.get("sha").asText(), "Child SHA mismatch") },
                { assertEquals("Second commit", child.get("message").asText(), "Child message mismatch") },
            )
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle non-existent commit`() {
            // Create a test query for a commit that doesn't exist in the test data
            // This should return an error
            graphQlTester
                .document(
                    """
                query {
                    commit(id: "999") {
                        id
                        sha
                        message
                    }
                }
            """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("Commit not found with id: 999") ?: false
                }.verify()
        }
    }
}
