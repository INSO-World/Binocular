package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.core.integration.base.TestDataProvider
import com.inso_world.binocular.web.graphql.base.GraphQlControllerTest
import com.inso_world.binocular.web.graphql.model.CommitDto
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * Test class for verifying the Commit resolver functionality.
 * This class extends GraphQlControllerTest to leverage the test data setup.
 */
internal class CommitResolverTest : GraphQlControllerTest() {
    @Autowired
    private lateinit var commitResolver: CommitResolver

    @Nested
    inner class BasicFunctionality {
        @Test
        fun `should retrieve commit with all fields`() {
            val sha = "a".repeat(40)
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$sha") {
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
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("msg1", result.get("message").asText(), "Commit message mismatch") },
                {
                    assertEquals(
                        "https://example.com/commit/$sha",
                        result.get("webUrl").asText(),
                        "Commit webUrl mismatch"
                    )
                },
                { assertEquals("main", result.get("branch").asText(), "Commit branch mismatch") },
                { assertEquals(10, result.get("stats").get("additions").asLong(), "Commit stats additions mismatch") },
                { assertEquals(5, result.get("stats").get("deletions").asLong(), "Commit stats deletions mismatch") },
            )
        }
    }

    @Nested
    inner class RelationshipTests {
        private val sha = TestDataProvider.testCommits[0].sha!!
        private val secondSha = TestDataProvider.testCommits[1].sha!!

        @Test
        fun `should retrieve commit with related builds`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$sha") {
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
                { assertEquals(TestDataProvider.testCommits[0].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[0].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify builds
            val builds = result.get("builds")
            assertNotNull(builds, "Builds should not be null")
            assertEquals(1, builds.size(), "Should have 1 build")

            val build = builds.get(0)
            assertAll(
                { assertEquals(TestDataProvider.testBuilds[0].id, build.get("id").asText(), "Build ID mismatch") },
                { assertEquals(TestDataProvider.testBuilds[0].sha, build.get("sha").asText(), "Build SHA mismatch") },
                { assertEquals(TestDataProvider.testBuilds[0].status, build.get("status").asText(), "Build status mismatch") },
                { assertEquals(TestDataProvider.testBuilds[0].ref, build.get("ref").asText(), "Build ref mismatch") },
                { assertEquals(TestDataProvider.testBuilds[0].tag, build.get("tag").asText(), "Build tag mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with related files`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$sha") {
                        id
                        sha
                        message
                        files {
                            count
                            page
                            perPage
                            data {
                                file {
                                    id
                                    path
                                    webUrl
                                    maxLength
                                }
                            }
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
                { assertEquals(TestDataProvider.testCommits[0].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[0].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify files connection
            val filesConnection = result.get("files")
            assertNotNull(filesConnection, "Files connection should not be null")
            val files = filesConnection.get("data")
            assertNotNull(files, "Files data should not be null")
            assertEquals(1, files.size(), "Should have 1 file")

            val file = files.get(0).get("file")
            assertAll(
                { assertEquals(TestDataProvider.testFiles[0].id, file.get("id").asText(), "File ID mismatch") },
                { assertEquals(TestDataProvider.testFiles[0].path, file.get("path").asText(), "File path mismatch") },
                { assertEquals(TestDataProvider.testFiles[0].webUrl, file.get("webUrl").asText(), "File webUrl mismatch") },
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
                    commit(sha: "$sha") {
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
                { assertEquals(TestDataProvider.testCommits[0].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[0].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify modules
            val modules = result.get("modules")
            assertNotNull(modules, "Modules should not be null")
            assertEquals(1, modules.size(), "Should have 1 module")

            val module = modules.get(0)
            assertAll(
                { assertEquals(TestDataProvider.testModules[0].id, module.get("id").asText(), "Module ID mismatch") },
                { assertEquals(TestDataProvider.testModules[0].path, module.get("path").asText(), "Module path mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with related users`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$sha") {
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
                { assertEquals(TestDataProvider.testCommits[0].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[0].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify users
            val users = result.get("users")
            assertNotNull(users, "Users should not be null")
            assertEquals(1, users.size(), "Should have 1 user")

            val user = users.get(0)
            assertAll(
                { assertTrue(user.get("id").isNull, "User ID should be null") },
                {
                    assertEquals(
                        TestDataProvider.testUsers[0].gitSignature,
                        user.get("gitSignature").asText(),
                        "User gitSignature mismatch"
                    )
                },
            )
        }

        @Test
        fun `should retrieve commit with related issues`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$sha") {
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
                { assertEquals(TestDataProvider.testCommits[0].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[0].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify issues
            val issues = result.get("issues")
            assertNotNull(issues, "Issues should not be null")
            assertEquals(1, issues.size(), "Should have 1 issue")

            val issue = issues.get(0)
            assertAll(
                { assertEquals(TestDataProvider.testIssues[0].id, issue.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(TestDataProvider.testIssues[0].platformIid, issue.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals(TestDataProvider.testIssues[0].title, issue.get("title").asText(), "Issue title mismatch") },
                {
                    assertEquals(
                        TestDataProvider.testIssues[0].description,
                        issue.get("description").asText(),
                        "Issue description mismatch"
                    )
                },
                { assertEquals(TestDataProvider.testIssues[0].state, issue.get("state").asText(), "Issue state mismatch") },
            )
        }

        @Test
        fun `should retrieve commit with parent relationships`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$secondSha") {
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
                { assertEquals(TestDataProvider.testCommits[1].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(secondSha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[1].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify parents
            val parents = result.get("parents")
            assertNotNull(parents, "Parents should not be null")
            assertEquals(1, parents.size(), "Should have 1 parent")

            val parentSha = parents.get(0).asText()
            assertEquals(sha, parentSha, "Parent SHA should match first commit")
        }

        @Test
        fun `should retrieve commit with child relationships`() {
            val result: JsonNode =
                graphQlTester
                    .document(
                        """
                query {
                    commit(sha: "$sha") {
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
                { assertEquals(TestDataProvider.testCommits[0].id, result.get("id").asText(), "Commit ID mismatch") },
                { assertEquals(sha, result.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[0].message, result.get("message").asText(), "Commit message mismatch") },
            )

            // Verify children
            val children = result.get("children")
            assertNotNull(children, "Children should not be null")
            assertEquals(1, children.size(), "Should have 1 child")

            val child = children.get(0)
            assertAll(
                { assertEquals(TestDataProvider.testCommits[1].id, child.get("id").asText(), "Child ID mismatch") },
                { assertEquals(secondSha, child.get("sha").asText(), "Child SHA mismatch") },
                { assertEquals(TestDataProvider.testCommits[1].message, child.get("message").asText(), "Child message mismatch") },
            )
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `builds should return empty list when commit id is null`() {
            // Arrange
            val commit = CommitDto(id = null, sha = "abc")

            // Act
            val result = commitResolver.builds(commit)

            // Assert
            assertTrue(result.isEmpty(), "Builds list should be empty when commit ID is null")
        }

        @Test
        fun `files should return empty connection when commit id is null`() {
            // Arrange
            val commit = CommitDto(id = null, sha = "abc")

            // Act
            val result = commitResolver.files(commit, null, null, null)

            // Assert
            assertTrue(result.data.isEmpty(), "Files data should be empty when commit ID is null")
            assertEquals(0, result.count, "Count should be 0")
        }

        @Test
        fun `should handle non-existent commit`() {
            graphQlTester
                .document(
                    """
            query {
                commit(sha: "nonexistent-sha") {
                    id
                }
            }
            """
                ).execute()
                .errors()
                .expect { error ->
                    error.message?.contains("Commit not found") == true
                }
        }
    }
}
