package com.inso_world.binocular.web.graphql.resolver

import com.fasterxml.jackson.databind.JsonNode
import com.inso_world.binocular.web.BaseDbTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for verifying the Issue resolver functionality.
 * This class extends BaseDbTest to leverage the test data setup.
 */
class IssueResolverTest : BaseDbTest() {

    @Nested
    inner class BasicFunctionality {
        @Test
        fun `should retrieve issue with all fields`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    issue(id: "1") {
                        id
                        iid
                        title
                        description
                        state
                        webUrl
                        labels
                    }
                }
            """)
                .execute()
                .path("issue")
                .entity(JsonNode::class.java)
                .get()

            // Verify issue data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, result.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", result.get("title").asText(), "Issue title mismatch") },
                { assertEquals("Users are unable to log in...", result.get("description").asText(), "Issue description mismatch") },
                { assertEquals("open", result.get("state").asText(), "Issue state mismatch") },
                { assertEquals("https://example.com/issues/101", result.get("webUrl").asText(), "Issue webUrl mismatch") },
                { assertTrue(result.get("labels").isArray(), "Labels should be an array") },
                { assertEquals("bug", result.get("labels").get(0).asText(), "First label mismatch") },
                { assertEquals("high-priority", result.get("labels").get(1).asText(), "Second label mismatch") }
            )
        }
    }

    @Nested
    inner class RelationshipTests {
        @Test
        fun `should retrieve issue with related accounts`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    issue(id: "1") {
                        id
                        iid
                        title
                        description
                        state
                        webUrl
                        accounts {
                            id
                            platform
                            login
                            name
                        }
                    }
                }
            """)
                .execute()
                .path("issue")
                .entity(JsonNode::class.java)
                .get()

            // Verify issue data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, result.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", result.get("title").asText(), "Issue title mismatch") },
                { assertEquals("Users are unable to log in...", result.get("description").asText(), "Issue description mismatch") },
                { assertEquals("open", result.get("state").asText(), "Issue state mismatch") },
                { assertEquals("https://example.com/issues/101", result.get("webUrl").asText(), "Issue webUrl mismatch") }
            )

            // Verify accounts
            val accounts = result.get("accounts")
            assertNotNull(accounts, "Accounts should not be null")
            assertEquals(1, accounts.size(), "Should have 1 account")

            val account = accounts.get(0)
            assertAll(
                { assertEquals("1", account.get("id").asText(), "Account ID mismatch") },
                { assertEquals("GitHub", account.get("platform").asText(), "Account platform mismatch") },
                { assertEquals("user1", account.get("login").asText(), "Account login mismatch") },
                { assertEquals("User One", account.get("name").asText(), "Account name mismatch") }
            )
        }

        @Test
        fun `should retrieve issue with related commits`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    issue(id: "1") {
                        id
                        iid
                        title
                        commits {
                            id
                            sha
                            message
                        }
                    }
                }
            """)
                .execute()
                .path("issue")
                .entity(JsonNode::class.java)
                .get()

            // Verify issue data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, result.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", result.get("title").asText(), "Issue title mismatch") }
            )

            // Verify commits
            val commits = result.get("commits")
            assertNotNull(commits, "Commits should not be null")
            assertEquals(1, commits.size(), "Should have 1 commit")

            val commit = commits.get(0)
            assertAll(
                { assertEquals("1", commit.get("id").asText(), "Commit ID mismatch") },
                { assertEquals("abc123", commit.get("sha").asText(), "Commit SHA mismatch") },
                { assertEquals("First commit", commit.get("message").asText(), "Commit message mismatch") }
            )
        }

        @Test
        fun `should retrieve issue with related milestones`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    issue(id: "1") {
                        id
                        iid
                        title
                        milestones {
                            id
                            iid
                            title
                            description
                        }
                    }
                }
            """)
                .execute()
                .path("issue")
                .entity(JsonNode::class.java)
                .get()

            // Verify issue data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, result.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", result.get("title").asText(), "Issue title mismatch") }
            )

            // Verify milestones
            val milestones = result.get("milestones")
            assertNotNull(milestones, "Milestones should not be null")
            assertEquals(1, milestones.size(), "Should have 1 milestone")

            val milestone = milestones.get(0)
            assertAll(
                { assertEquals("1", milestone.get("id").asText(), "Milestone ID mismatch") },
                { assertEquals(201, milestone.get("iid").asInt(), "Milestone IID mismatch") },
                { assertEquals("Release 1.0", milestone.get("title").asText(), "Milestone title mismatch") },
                { assertEquals("First stable release", milestone.get("description").asText(), "Milestone description mismatch") }
            )
        }

        @Test
        fun `should retrieve issue with related notes`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    issue(id: "1") {
                        id
                        iid
                        title
                        notes {
                            id
                            body
                        }
                    }
                }
            """)
                .execute()
                .path("issue")
                .entity(JsonNode::class.java)
                .get()

            // Verify issue data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, result.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", result.get("title").asText(), "Issue title mismatch") }
            )

            // Verify notes
            val notes = result.get("notes")
            assertNotNull(notes, "Notes should not be null")
            assertEquals(1, notes.size(), "Should have 1 note")

            val note = notes.get(0)
            assertAll(
                { assertEquals("1", note.get("id").asText(), "Note ID mismatch") },
                { assertEquals("This is a comment", note.get("body").asText(), "Note body mismatch") }
            )
        }

        @Test
        fun `should retrieve issue with related users`() {
            val result: JsonNode = graphQlTester.document("""
                query {
                    issue(id: "1") {
                        id
                        iid
                        title
                        users {
                            id
                            gitSignature
                        }
                    }
                }
            """)
                .execute()
                .path("issue")
                .entity(JsonNode::class.java)
                .get()

            // Verify issue data
            assertAll(
                { assertEquals("1", result.get("id").asText(), "Issue ID mismatch") },
                { assertEquals(101, result.get("iid").asInt(), "Issue IID mismatch") },
                { assertEquals("Fix bug in login flow", result.get("title").asText(), "Issue title mismatch") }
            )

            // Verify users
            val users = result.get("users")
            assertNotNull(users, "Users should not be null")
            assertEquals(1, users.size(), "Should have 1 user")

            val user = users.get(0)
            assertAll(
                { assertEquals("1", user.get("id").asText(), "User ID mismatch") },
                { assertEquals("John Doe <john.doe@example.com>", user.get("gitSignature").asText(), "User gitSignature mismatch") }
            )
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `should handle non-existent issue`() {
            // Create a test query for an issue that doesn't exist in the test data
            // This should return an error
            graphQlTester.document("""
                query {
                    issue(id: "999") {
                        id
                        iid
                        title
                    }
                }
            """)
                .execute()
                .errors()
                .expect { error ->
                    error.message?.contains("Issue not found with id: 999") ?: false
                }
                .verify()
        }
    }
}
