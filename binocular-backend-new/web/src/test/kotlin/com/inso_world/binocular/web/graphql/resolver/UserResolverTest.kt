package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.graphql.base.GraphQlControllerTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the User resolver functionality.
 * This class extends GraphQlControllerTest to leverage the test data setup.
 */
internal class UserResolverTest : GraphQlControllerTest() {
    @Nested
    inner class BasicFunctionality {
        @Test
        fun `should retrieve user with all fields`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    user(id: "1") {
                        id
                        gitSignature
                    }
                }
            """,
                    ).execute()
                    .path("user")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify user data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "User ID mismatch") },
                { assertEquals("John Doe <john.doe@example.com>", result.get("gitSignature").asText(), "User gitSignature mismatch") },
            )
        }
    }

    @Nested
    inner class RelationshipTests {
        @Test
        fun `should retrieve user with related commits`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    user(id: "1") {
                        id
                        gitSignature
                        commits {
                            id
                            sha
                            message
                        }
                    }
                }
            """,
                    ).execute()
                    .path("user")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify user data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "User ID mismatch") },
                { assertEquals("John Doe <john.doe@example.com>", result.get("gitSignature").asText(), "User gitSignature mismatch") },
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
        fun `should retrieve user with related issues`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    user(id: "1") {
                        id
                        gitSignature
                        issues {
                            id
                            iid
                            title
                        }
                    }
                }
            """,
                    ).execute()
                    .path("user")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify user data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "User ID mismatch") },
                { assertEquals("John Doe <john.doe@example.com>", result.get("gitSignature").asText(), "User gitSignature mismatch") },
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
            )
        }

        @Test
        fun `should retrieve user with related files`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    user(id: "1") {
                        id
                        gitSignature
                        files {
                            id
                            path
                            webUrl
                        }
                    }
                }
            """,
                    ).execute()
                    .path("user")
                    .entity(JsonNode::class.java)
                    .get()

            // Verify user data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "User ID mismatch") },
                { assertEquals("John Doe <john.doe@example.com>", result.get("gitSignature").asText(), "User gitSignature mismatch") },
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
            )
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle non-existent user`() {
            // Create a test query for a user that doesn't exist in the test data
            // This should return an error
            graphQlTester
                .document(
                    """
                query {
                    user(id: "999") {
                        id
                        gitSignature
                    }
                }
            """,
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("User not found with id: 999") ?: false
                }.verify()
        }
    }
}
